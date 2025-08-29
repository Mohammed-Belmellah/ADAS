import { useEffect, useState, useMemo } from "react";
import { useParams, Link } from "react-router-dom";
import { getSessionReport, type SessionReportDTO } from "../../api/sessions";
import {
  ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from "recharts";

const PALETTE = ["#0088FE","#00C49F","#FFBB28","#FF8042","#A28BFF","#FF5C8D"];

function fmtSec(s: number) {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = Math.floor(s % 60);
  if (h) return `${h}h ${m}m`;
  if (m) return `${m}m ${sec}s`;
  return `${sec}s`;
}
const nice = (s: string) =>
  s.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());

export default function SessionReportPage() {
  const { sessionId } = useParams();
  const [data, setData] = useState<SessionReportDTO | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!sessionId) return;
    (async () => {
      setLoading(true);
      setErr(null);
      try {
        const r = await getSessionReport(sessionId);
        setData(r);
      } catch (e: any) {
        setErr(e?.message ?? "Failed to load report");
      } finally {
        setLoading(false);
      }
    })();
  }, [sessionId]);

  // Hide emotions that are (near) 0% so labels/legend don't show “Happy 0%”
  const emotionPie = useMemo(() => {
    const EPS = 0.0005; // ~0.05% threshold
    const share = data?.emotionShare || {};
    return Object.entries(share)
      .filter(([, v]) => (v ?? 0) > EPS) // filter near-zero
      .map(([k, v]) => ({
        name: nice(k),
        value: Number(((v || 0) * 100).toFixed(1)), // percent with 0.1 precision
      }))
      .sort((a, b) => b.value - a.value);
  }, [data?.emotionShare]);

  const confBars = useMemo(() => {
    const conf = data?.confAvg || {};
    return Object.entries(conf).map(([k, v]) => ({
      name: nice(k),
      value: Number((v || 0).toFixed(3)),
    }));
  }, [data?.confAvg]);

  if (loading) return <div>Loading report…</div>;
  if (err) return <div className="alert alert-danger">{err}</div>;
  if (!data) return null;

  return (
    <div>
      <div className="d-flex align-items-center justify-content-between mb-3">
        <h3 className="mb-0">Session Report</h3>
        <Link to="/admin" className="btn btn-outline-secondary btn-sm">Back</Link>
      </div>

      <div className="card mb-3">
        <div className="card-body row g-3">
          <Kpi label="Session ID" value={data.sessionId.slice(0, 8) + "…"} />
          <Kpi label="Start" value={new Date(data.startTime).toLocaleString()} />
          <Kpi label="End" value={new Date(data.endTime).toLocaleString()} />
          <Kpi label="Duration" value={fmtSec(data.durationSec)} />
          <Kpi label="Dominant Emotion" value={nice(data.dominantEmotion)} />
          <Kpi label="Safety Score" value={`${data.safetyEmotionScore}%`} />
          <Kpi label="Fatigue Index" value={`${data.fatigueIndex}%`} />
          <Kpi label="Stability Index" value={`${data.stabilityIndex}%`} />
          <Kpi label="Total Alerts" value={data.totalAlerts} />
          <Kpi label="Unresolved Alerts" value={data.unresolvedAlerts} />
        </div>
      </div>

      <div className="row g-3">
        <div className="col-lg-6">
          <div className="card h-100">
            <div className="card-header">Emotion Share</div>
            <div className="card-body" style={{ height: 320 }}>
              {emotionPie.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={emotionPie}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius={45}
                      outerRadius={100}
                      labelLine={false}
                      label={({ name, value }) => `${name} ${value}%`}
                    >
                      {emotionPie.map((_, i) => (
                        <Cell key={i} fill={PALETTE[i % PALETTE.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(v: number) => `${v}%`} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="text-muted">No emotion data</div>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-6">
          <div className="card h-100">
            <div className="card-header">Average Confidence</div>
            <div className="card-body" style={{ height: 320 }}>
              {confBars.length ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={confBars} margin={{ top: 8, right: 8, bottom: 8, left: 8 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis domain={[0, 1]} />
                    <Tooltip formatter={(v: number) => v.toFixed(3)} />
                    <Bar dataKey="value" fill="#0088FE" />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="text-muted">No data</div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="card mt-3">
        <div className="card-header">Alerts</div>
        <div className="card-body">
          {Object.keys(data.alertsCount || {}).length === 0 ? (
            <div className="text-muted">No alerts.</div>
          ) : (
            <ul className="list-group list-group-flush">
              {Object.entries(data.alertsCount)
                .sort((a, b) => b[1] - a[1])
                .map(([t, c]) => (
                  <li key={t} className="list-group-item d-flex justify-content-between">
                    <span>{nice(t)}</span>
                    <span className="badge text-bg-secondary">{c}</span>
                  </li>
                ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}

function Kpi({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="col-md-3">
      <div className="border rounded p-3 h-100 text-center">
        <div className="text-muted small">{label}</div>
        <div className="fs-4">{value}</div>
      </div>
    </div>
  );
}
