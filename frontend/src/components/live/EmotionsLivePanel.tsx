import { useMemo } from "react";
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip
} from "recharts";

const nice = (s: string) =>
  s.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());

export default function EmotionsLivePanel({ emotions }: { emotions: Record<string, number> }) {
  const chartData = useMemo(
    () =>
      Object.entries(emotions ?? {})
        .map(([name, v]) => ({ name: nice(name), value: clamp01(v ?? 0) }))
        .filter((d) => d.value > 0) // drop zeroes (optional)
        .sort((a, b) => b.value - a.value),
    [emotions]
  );

  const top = useMemo(() => {
    let best: { name: string; value: number } | null = null;
    for (const [k, v] of Object.entries(emotions ?? {})) {
      const vv = clamp01(v ?? 0);
      if (vv <= 0) continue;
      if (!best || vv > best.value) best = { name: nice(k), value: vv };
    }
    return best;
  }, [emotions]);

  return (
    <div className="card h-100">
      <div className="card-header d-flex align-items-center justify-content-between">
        <span>Driver Emotions (live)</span>
      </div>
      <div className="card-body" style={{ height: 320 }}>
        {chartData.length ? (
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 8, right: 8, bottom: 8, left: 8 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis domain={[0, 1]} tickFormatter={(v) => (v * 100).toFixed(0) + "%"} />
              <Tooltip formatter={(v: number) => (v * 100).toFixed(1) + "%"} />
              <Bar dataKey="value" fill="#0088FE" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-muted">Waiting for data…</div>
        )}
      </div>
      <div className="card-footer">
        <strong>Current state:</strong>{" "}
        {top ? (
          <>
            {top.name} <span className="text-muted">({(top.value * 100).toFixed(1)}%)</span>
          </>
        ) : (
          "—"
        )}
      </div>
    </div>
  );
}

function clamp01(n: number) {
  if (Number.isNaN(n)) return 0;
  return Math.max(0, Math.min(1, n));
}
