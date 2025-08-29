// src/hooks/useStompTopics.ts
import { useEffect, useRef, useState } from "react";
import type { IMessage } from "@stomp/stompjs";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "../auth/keycloak";

type Options = {
  debug?: boolean;
  onMessage?: (topic: string, body: any, raw?: IMessage) => void;
};

export function useStompTopics(
  wsHttpUrl: string | undefined,
  topics: string[],
  opts: Options = {}
) {
  const { debug = false, onMessage } = opts;
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const subsRef = useRef<Record<string, () => void>>({});

  useEffect(() => {
    if (!topics || topics.length === 0) return;

    let cancelled = false;
    let client: Client | null = null;

    (async () => {
      const token = await getAccessToken();               // ← Keycloak token
      if (cancelled) return;

      const url = wsHttpUrl || `${window.location.origin}/ws`;
      if (debug) console.log("[STOMP] connecting to", url);

      client = new Client({
        webSocketFactory: () => new SockJS(url),
        reconnectDelay: 3000,
        // The important part: send Bearer in STOMP CONNECT headers
        connectHeaders: { Authorization: `Bearer ${token}` },
        debug: (str) => debug && console.log("[STOMP]", str),

        beforeConnect: async () => {
          // Optional: refresh token before each (re)connect
          const fresh = await getAccessToken();
          client!.connectHeaders = { Authorization: `Bearer ${fresh}` };
        },

        onConnect: () => {
          setConnected(true);
          if (debug) console.log("[STOMP] connected");

          topics.forEach((dest) => {
            const sub = client!.subscribe(dest, (msg) => {
              let parsed: any = msg.body;
              try { parsed = JSON.parse(msg.body); } catch {}
              debug && console.log("[STOMP] message", dest, parsed);
              onMessage?.(dest, parsed, msg);
            });
            subsRef.current[dest] = () => sub.unsubscribe();
            debug && console.log("[STOMP] subscribed", dest);
          });
        },

        onStompError: (frame) => {
          console.error("[STOMP] broker error", frame);
        },
        onDisconnect: () => {
          setConnected(false);
          debug && console.log("[STOMP] disconnected");
        },
        onWebSocketClose: () => {
          setConnected(false);
          debug && console.log("[STOMP] socket closed");
        },
      });

      clientRef.current = client;
      client.activate();
    })();

    return () => {
      cancelled = true;
      Object.values(subsRef.current).forEach((u) => u?.());
      subsRef.current = {};
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [wsHttpUrl, JSON.stringify(topics)]);

  return { connected };
}
