import { useEffect, useMemo, useState } from "react";
import { getDriverPerformance } from "../../api/analytics";
import type { DriverPerformanceDTO } from "../../api/analytics";
import DriverPerformanceCharts from "./DriverPerformanceCharts";

type Props = { driverId: string };

function fmtSec(s: number) {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = Math.floor(s % 60);
  return h ? `${h}h ${m}m` : m ? `${m}m ${sec}s` : `${sec}s`;
}

export default function DriverPerformancePanel({ driverId }: Props) {
  const [from, setFrom] = useState<string>("");
  const [to, setTo] = useState<string>("");
  const [data, setData] = useState<DriverPerformanceDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setErr(null);
    try {
      const fromISO = from ? new Date(from).toISOString() : undefined;
      const toISO = to ? new Date(to).toISOString() : undefined;
      const res = await getDriverPerformance(driverId, fromISO, toISO);
      setData(res);
    } catch (e: any) {
      setErr(e?.message ?? "Failed to load");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [driverId]);

  const safetyTrend = useMemo(() => {
    const a = data?.safetyLast30d;
    const b = data?.safetyPrev30d;
    if (a == null || b == null) return null;
    const diff = a - b;
    return { a, b, diff, sign: diff === 0 ? "equal" : diff > 0 ? "up" : "down" };
  }, [data]);

  return (
    <div className="card">
      <div className="card-header d-flex align-items-center gap-2 flex-wrap">
        <strong>Driver Performance</strong>
        <div className="ms-auto d-flex align-items-center gap-2">
          <input type="datetime-local" className="form-control" value={from} onChange={e => setFrom(e.target.value)} />
          <span>to</span>
          <input type="datetime-local" className="form-control" value={to} onChange={e => setTo(e.target.value)} />
          <button className="btn btn-outline-primary" onClick={load} disabled={loading}>
            {loading ? "Loading…" : "Refresh"}
          </button>
        </div>
      </div>

      <div className="card-body">
        {err && <div className="alert alert-danger">{err}</div>}
        {!err && !data && <div>Loading…</div>}

        {data && (
          <>
            <div className="text-muted mb-3 small">
              Range:&nbsp;
              {data.from ? new Date(data.from).toLocaleString() : "—"} &nbsp;→&nbsp;
              {data.to ? new Date(data.to).toLocaleString() : "—"}
            </div>

            {/* KPIs */}
            <div className="row g-3 mb-3">
              <KPI label="Sessions" value={data.sessionsCount} />
              <KPI label="Total Duration" value={fmtSec(data.totalDurationSec)} />
              <KPI label="Avg Session" value={fmtSec(data.avgSessionDurationSec)} />
              <KPI label="Latest End" value={data.latestSessionEnd ? new Date(data.latestSessionEnd).toLocaleString() : "—"} />
              <KPI label="Safety Avg" value={`${Math.round(data.safetyAvg)}%`} />
              <KPI label="Fatigue Index Avg" value={`${Math.round(data.fatigueIndexAvg)}%`} />
              <KPI label="Stability Index Avg" value={`${Math.round(data.stabilityIndexAvg)}%`} />
              <KPI label="Peaks Total" value={data.peaksTotal} />
              <KPI label="Max Fatigue Streak" value={fmtSec(data.maxFatigueStreakSec)} />
              <KPI label="Alerts Total" value={data.alertsTotal} />
            </div>

            {/* Safety trend */}
            {safetyTrend && (
              <div className={`alert ${safetyTrend.sign === "up" ? "alert-success" : safetyTrend.sign === "down" ? "alert-warning" : "alert-secondary"}`}>
                Safety last 30d: <strong>{Math.round(safetyTrend.a)}%</strong> vs prev 30d: <strong>{Math.round(safetyTrend.b)}%</strong> &nbsp;
                ({safetyTrend.diff > 0 ? "▲" : safetyTrend.diff < 0 ? "▼" : "—"} {Math.abs(Math.round(safetyTrend.diff))}%)
              </div>
            )}

            {/* Charts (pie + horizontal bar) */}
            <DriverPerformanceCharts perf={data} />

            {/* Dev aid: raw payload */}
            <details className="mt-3">
              <summary>Raw response</summary>
              <pre className="mt-2 bg-light p-2 rounded" style={{ whiteSpace: "pre-wrap" }}>
                {JSON.stringify(data, null, 2)}
              </pre>
            </details>
          </>
        )}
      </div>
    </div>
  );
}

function KPI({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="col-md-3">
      <div className="card shadow-sm h-100">
        <div className="card-body text-center">
          <div className="text-muted small">{label}</div>
          <div className="display-6">{value}</div>
        </div>
      </div>
    </div>
  );
}
