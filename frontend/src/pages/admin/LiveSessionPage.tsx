// src/pages/admin/LiveSessionPage.tsx
import { useMemo, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useStompTopics } from "../../hooks/useStompTopics";
import EmotionsLivePanel from "../../components/live/EmotionsLivePanel";
import AlertsLivePanel from "../../components/live/AlertsLivePanel";
import type { LiveAlert } from "../../components/live/AlertsLivePanel";
import AlertsHistoryPanel from "../../components/live/AlertsHistoryPanel";

const WS_HTTP = import.meta.env.VITE_WS_HTTP_URL as string | undefined;

type EmotionsPayload =
  | { emotions: Record<string, number>; confidences?: number[]; sessionId?: string; ts?: string }
  | { labels: string[]; confidences: number[]; sessionId?: string; ts?: string };

export default function LiveSessionPage() {
  const { sessionId } = useParams<{ sessionId: string }>();

  const [emotions, setEmotions] = useState<Record<string, number>>({});
  const [alertCounts, setAlertCounts] = useState<Record<string, number>>({});
  const [recentAlerts, setRecentAlerts] = useState<LiveAlert[]>([]);

  const topics = useMemo(() => {
    if (!sessionId) return [];
    return [
      `/topic/emotions.session.${sessionId}`,
      `/topic/alerts.session.${sessionId}`,
    ];
  }, [sessionId]);

  const { connected } = useStompTopics(WS_HTTP, topics, {
    debug: true, // turn off once verified
    onMessage: (topic, rawBody) => {
      // Parse if broker delivers a string
      const body =
        typeof rawBody === "string"
          ? (() => {
              try {
                return JSON.parse(rawBody);
              } catch {
                return rawBody;
              }
            })()
          : rawBody;

      // ---- EMOTIONS ----
      if (topic.startsWith("/topic/emotions.session.")) {
        const sample: EmotionsPayload =
          Array.isArray(body) ? (body as any[])[(body as any[]).length - 1] : body;

        const map: Record<string, number> = {};

        // A) { emotions: ["sad",...], confidences: [0.7,...] }
        if (Array.isArray((sample as any)?.emotions) && Array.isArray((sample as any)?.confidences)) {
          const labels: string[] = (sample as any).emotions;
          const vals: number[] = (sample as any).confidences;
          labels.forEach((label, i) => {
            const v = Number(vals[i] ?? 0);
            if (!Number.isNaN(v)) map[label] = v;
          });
        }
        // B) { labels: [...], confidences: [...] }
        else if (Array.isArray((sample as any)?.labels) && Array.isArray((sample as any)?.confidences)) {
          const labels: string[] = (sample as any).labels;
          const vals: number[] = (sample as any).confidences;
          labels.forEach((label, i) => {
            const v = Number(vals[i] ?? 0);
            if (!Number.isNaN(v)) map[label] = v;
          });
        }
        // C) { emotions: { sad: 0.2, ... } }
        else if ((sample as any)?.emotions && typeof (sample as any).emotions === "object") {
          Object.entries((sample as any).emotions).forEach(([k, v]) => {
            const n = Number(v);
            if (!Number.isNaN(n)) map[k] = n;
          });
        }

        // Normalize if >1
        const max = Math.max(0, ...Object.values(map));
        const normalized =
          max > 1
            ? Object.fromEntries(Object.entries(map).map(([k, v]) => [k, v / max]))
            : map;

        setEmotions(normalized);
        return;
      }

      // ---- ALERTS ----
      if (topic.startsWith("/topic/alerts.session.")) {
        const arr: LiveAlert[] = Array.isArray(body) ? (body as any) : [body];
        setAlertCounts((prev) => {
          const next = { ...prev };
          for (const a of arr) {
            const key = (a?.type ?? "UNKNOWN").toString();
            next[key] = (next[key] || 0) + 1;
          }
          return next;
        });
        setRecentAlerts((prev) => [...arr, ...prev].slice(0, 30));
      }
    },
  });

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-3">
        <h3 className="mb-0">Live Surveillance</h3>
        <div className="d-flex align-items-center gap-3">
          <span className={`badge ${connected ? "text-bg-success" : "text-bg-secondary"}`}>
            {connected ? "Connected" : "Disconnected"}
          </span>
          <span className="text-muted small">Session: {sessionId?.slice(0, 8)}…</span>
          <Link to="/admin/active-sessions" className="btn btn-outline-secondary btn-sm">
            Back
          </Link>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-lg-6">
          <EmotionsLivePanel emotions={emotions} />
        </div>
        <div className="col-lg-6">
          <AlertsLivePanel counts={alertCounts} recent={recentAlerts} />
          {sessionId && <AlertsHistoryPanel sessionId={sessionId} />}
        </div>
      </div>
    </div>
  );
}
