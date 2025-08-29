import { useEffect, useMemo, useState } from "react";
import { getSessionAlerts, type AlertDTO } from "../../api/sessions";

const nice = (s: string) =>
  s.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase());

export default function AlertsHistoryPanel({
  sessionId,
  defaultLimit = 50,
}: { sessionId: string; defaultLimit?: number }) {
  const [items, setItems] = useState<AlertDTO[]>([]);
  const [limit, setLimit] = useState(defaultLimit);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function load() {
    try {
      setLoading(true); setErr(null);
      const data = await getSessionAlerts(sessionId, limit);
      // newest first
      setItems([...data].sort(
        (a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      ));
    } catch (e: any) {
      setErr(e?.message ?? "Failed to load alert history");
      setItems([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [sessionId, limit]);

  const grouped = useMemo(() => {
    const m = new Map<string, AlertDTO[]>();
    items.forEach(a => {
      const k = a.type || "UNKNOWN";
      if (!m.has(k)) m.set(k, []);
      m.get(k)!.push(a);
    });
    return Array.from(m.entries())
      .sort((a,b) => b[1].length - a[1].length); // by volume desc
  }, [items]);

  return (
    <div className="card mt-3">
      <div className="card-header d-flex align-items-center justify-content-between">
        <span>Alerts</span>
        <div className="d-flex align-items-center gap-2">
          <select
            className="form-select form-select-sm"
            style={{ width: 110 }}
            value={limit}
            onChange={(e) => setLimit(Number(e.target.value))}
          >
            {[20,50,100,200].map(n => <option key={n} value={n}>{n} latest</option>)}
          </select>
          <button className="btn btn-sm btn-outline-secondary" onClick={load} disabled={loading}>
            {loading ? "Refreshing…" : "Refresh"}
          </button>
        </div>
      </div>

      <div className="list-group list-group-flush">
        {err && <div className="list-group-item text-danger">{err}</div>}
        {!err && !loading && items.length === 0 && (
          <div className="list-group-item text-muted">No past alerts for this session.</div>
        )}

        {grouped.map(([type, arr]) => (
          <div key={type} className="list-group-item">
            <div className="d-flex align-items-center justify-content-between">
              <div className="fw-semibold">{nice(type)}</div>
              <span className="badge text-bg-secondary">{arr.length}</span>
            </div>
            <ul className="mt-2 mb-0 ps-3 small text-muted">
              {arr.slice(0, 5).map(a => (
                <li key={a.id}>
                  {new Date(a.createdAt).toLocaleString()}
                  {a.message ? ` — ${a.message}` : ""}
                </li>
              ))}
              {arr.length > 5 && <li>… {arr.length - 5} more</li>}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}
