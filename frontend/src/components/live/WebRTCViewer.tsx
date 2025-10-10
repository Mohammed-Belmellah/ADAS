import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import "./webrtc.css";

type Props = {
  /** Full URL to your aiortc server's offer endpoint, e.g. http://localhost:8090/offer */
  offerUrl: string;
  /** Preferred codec; must match your server's ?codec= handling */
  codec?: "vp8" | "h264";
  /** Auto start on mount */
  autoStart?: boolean;
  /** Show minimal UI controls (Start/Stop + codec selector) */
  showControls?: boolean;
  /** Extra className on outer wrapper */
  className?: string;
  /** Optional bearer token if your /offer requires auth */
  token?: string;
  /** Optional: called with pc.connectionState updates */
  onStateChange?: (state: RTCPeerConnectionState) => void;
  /** Optional: show browser stats under the video */
  showStats?: boolean;
};

const WebRTCViewer: React.FC<Props> = ({
  offerUrl,
  codec = "vp8",
  autoStart = true,
  showControls = true,
  className,
  token,
  onStateChange,
  showStats = false,
}) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const pcRef = useRef<RTCPeerConnection | null>(null);

  const [connecting, setConnecting] = useState(false);
  const [state, setState] = useState<RTCPeerConnectionState>("new");
  const [chosenCodec, setChosenCodec] = useState<string | null>(null);
  const [bytes, setBytes] = useState<number>(0);
  const [frames, setFrames] = useState<number>(0);
  const [err, setErr] = useState<string | null>(null);
  const [codecOverride, setCodecOverride] = useState<"vp8" | "h264">(codec);

  const offerUrlWithCodec = useMemo(() => {
    const u = new URL(offerUrl);
    u.searchParams.set("codec", codecOverride);
    return u.toString();
  }, [offerUrl, codecOverride]);

  const stop = useCallback(() => {
    setConnecting(false);
    setErr(null);
    try {
      const pc = pcRef.current;
      if (pc) {
        pc.ontrack = null;
        pc.onconnectionstatechange = null;
        pc.close();
      }
    } catch {}
    pcRef.current = null;
    const v = videoRef.current;
    if (v) v.srcObject = null;
  }, []);

  const start = useCallback(async () => {
    stop();
    setConnecting(true);
    setChosenCodec(null);
    setBytes(0);
    setFrames(0);
    setErr(null);

    try {
      // For localhost, keep iceServers empty to avoid odd interfaces
      const pc = new RTCPeerConnection({ iceServers: [] });
      pcRef.current = pc;

      pc.onconnectionstatechange = () => {
        setState(pc.connectionState);
        onStateChange?.(pc.connectionState);
      };

      // Receive-only transceiver
      pc.addTransceiver("video", { direction: "recvonly" });

      // Attach incoming track
      pc.ontrack = (e) => {
        const v = videoRef.current;
        if (!v) return;
        v.srcObject = e.streams[0];
        v.play().catch(() => {});
      };

      // Offer → POST /offer → setRemoteDescription
      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);

      const headers: Record<string, string> = { "Content-Type": "application/json" };
      if (token) headers["Authorization"] = `Bearer ${token}`;

      const resp = await fetch(offerUrlWithCodec, {
        method: "POST",
        headers,
        body: JSON.stringify({
          sdp: pc.localDescription?.sdp,
          type: pc.localDescription?.type,
        }),
      });

      if (!resp.ok) {
        const raw = await resp.text().catch(() => "");
        throw new Error(`HTTP ${resp.status} ${resp.statusText}: ${raw}`);
      }

      const answer = await resp.json();
      await pc.setRemoteDescription(answer);

      // Try to read negotiated codec (best-effort)
      try {
        // On recvonly, check receivers
        const rec = pc.getReceivers().find((r) => r.track?.kind === "video");
        const params: any = rec?.getParameters?.();
        const mime = params?.codecs?.[0]?.mimeType ?? null;
        setChosenCodec(mime);
      } catch {
        setChosenCodec(null);
      }
    } catch (e: any) {
      setErr(e?.message || "Failed to start WebRTC");
      stop();
    } finally {
      setConnecting(false);
    }
  }, [offerUrlWithCodec, onStateChange, stop, token]);

  // Stats loop (browser-side only)
  useEffect(() => {
    if (!showStats) return;
    const id = setInterval(async () => {
      const pc = pcRef.current;
      if (!pc) return;
      try {
        const rep = await pc.getStats();
        let b = 0, f = 0;
        rep.forEach((s: any) => {
          if (s.type === "inbound-rtp" && s.kind === "video") {
            b = s.bytesReceived || 0;
            f = s.framesDecoded || 0;
          }
        });
        setBytes(b);
        setFrames(f);
      } catch {}
    }, 1000);
    return () => clearInterval(id);
  }, [showStats]);

  // Autostart
  useEffect(() => {
    if (!autoStart) return;
    start();
    return stop;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoStart, offerUrlWithCodec]);

  return (
    <div className={className}>
      {showControls && (
        <div className="webrtc-controls">
          <button onClick={start} disabled={connecting} className="btn btn-primary btn-sm">
            {connecting ? "Starting…" : "Start"}
          </button>
          <button onClick={stop} className="btn btn-outline-secondary btn-sm">
            Stop
          </button>
          <label className="ms-2">
            Codec:&nbsp;
            <select
              value={codecOverride}
              onChange={(e) => setCodecOverride(e.target.value as "vp8" | "h264")}
            >
              <option value="vp8">VP8</option>
              <option value="h264">H.264</option>
            </select>
          </label>
          <span className="text-muted small ms-2">
            State: <code>{state}</code>
            {chosenCodec ? <> · Codec: <code>{chosenCodec}</code></> : null}
          </span>
        </div>
      )}

      {/* 16:9 container (change to ratio-4x3 if you want) */}
      <div className="video-shell mb-3">
        <div className="ratio ratio-16x9">
          <video ref={videoRef} autoPlay playsInline muted />
        </div>
      </div>

      {err && (
        <pre className="webrtc-error">
          {err}
        </pre>
      )}

      {showStats && (
        <pre className="webrtc-stats">
{`bytesReceived: ${bytes}
framesDecoded: ${frames}`}
        </pre>
      )}
    </div>
  );
};

export default WebRTCViewer;
