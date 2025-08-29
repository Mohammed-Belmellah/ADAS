// src/components/drivers/DriverPerformanceCharts.tsx
import { useMemo } from "react";
import type { DriverPerformanceDTO } from "../../api/analytics";
import {
  ResponsiveContainer,
  PieChart, Pie, Cell, Tooltip, Legend,
} from "recharts";

const PALETTE = [
  "#0088FE", "#00C49F", "#FFBB28", "#FF8042",
  "#A28BFF", "#FF5C8D", "#7CC576", "#FF6F59",
];

// SURPRISED -> Surprised, EXTREME_FATIGUE -> Extreme Fatigue
const nice = (s: string) =>
  s.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());

export default function DriverPerformanceCharts({ perf }: { perf: DriverPerformanceDTO }) {
  // Pie data (percent with one decimal)
  const emotionData = useMemo(() => {
  const EPS = 0.0005; // ~0.05%. Hide slices that are effectively zero.
  return Object.entries(perf.emotionShareAvg || {})
    .filter(([, v]) => (v ?? 0) > EPS) // ⬅️ filter zeros
    .map(([name, v]) => ({
      name: nice(name),
      value: Math.round((v || 0) * 1000) / 10, // percent with 0.1 precision
    }))
    .sort((a, b) => b.value - a.value);
}, [perf.emotionShareAvg]);


  // Alerts list data
  const alertEntries = useMemo(
    () =>
      Object.entries(perf.alertsByType || {})
        .map(([type, count]) => ({ type: nice(type), count: Number(count || 0) }))
        .sort((a, b) => b.count - a.count),
    [perf.alertsByType]
  );

  const totalAlerts = useMemo(
    () => alertEntries.reduce((sum, e) => sum + e.count, 0),
    [alertEntries]
  );

  const hasEmotions = emotionData.length > 0;
  const hasAlerts = totalAlerts > 0;

  return (
    <div className="row g-3 mt-2">
      {/* Emotion Share → Donut Pie */}
      <div className="col-lg-6">
        <div className="card h-100">
          <div className="card-header">Weighted Emotion Share (avg)</div>
          <div className="card-body" style={{ height: 320 }}>
            {hasEmotions ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart margin={{ top: 8, right: 8, bottom: 8, left: 8 }}>
                  <Pie
                    data={emotionData}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    innerRadius={45}
                    outerRadius={100}
                    labelLine={false}
                    label={({ name, value }) => `${name} ${Number(value).toFixed(1)}%`}
                  >
                    {emotionData.map((_, i) => (
                      <Cell key={i} fill={PALETTE[i % PALETTE.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v: any) => `${Number(v).toFixed(1)}%`} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-muted">No data</div>
            )}
          </div>
        </div>
      </div>

      {/* Alerts → KPI + By-type list (no chart) */}
      <div className="col-lg-6">
        <div className="card h-100">
          <div className="card-header">Alerts</div>
          <div className="card-body d-flex flex-column" style={{ minHeight: 320 }}>
            {/* KPI */}
            <div className="card text-center mb-3">
              <div className="card-header">Total Alerts</div>
              <div className="card-body">
                <div className="display-6 mb-0">{totalAlerts}</div>
                <div className="text-muted small">
                  {alertEntries.length} alert type{alertEntries.length === 1 ? "" : "s"}
                </div>
              </div>
            </div>

            {/* By type list */}
            {hasAlerts ? (
              <>
                <div className="fw-semibold mb-2">By Type</div>
                <ul className="list-group list-group-flush flex-grow-1">
                  {alertEntries.map((e) => (
                    <li
                      key={e.type}
                      className="list-group-item d-flex justify-content-between align-items-center"
                    >
                      <span>{e.type}</span>
                      <span className="d-flex align-items-center gap-2">
                        <span className="text-muted small">
                          {totalAlerts > 0 ? Math.round((e.count / totalAlerts) * 100) : 0}%
                        </span>
                        <span className="badge text-bg-primary">{e.count}</span>
                      </span>
                    </li>
                  ))}
                </ul>
              </>
            ) : (
              <div className="text-muted">No alerts</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
