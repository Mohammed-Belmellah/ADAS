import { useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  getActiveSessionsWithDetailsPaged,
  type PageActiveSessionDetailsDTO,
  type ActiveSessionDetailsDTO,
  type AlertDTO,
} from "../../api/sessions";

function fmtDT(iso?: string | null) {
  return iso ? new Date(iso).toLocaleString() : "—";
}
function fmtElapsedFrom(startIso?: string) {
  if (!startIso) return "—";
  const s = new Date(startIso).getTime();
  const sec = Math.max(0, Math.round((Date.now() - s) / 1000));
  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  const r = sec % 60;
  if (h) return `${h}h ${m}m`;
  if (m) return `${m}m ${r}s`;
  return `${r}s`;
}
const nice = (s: string) =>
  s.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());

export default function ActiveSessionsPage() {
  // server params
  const [companyId, setCompanyId] = useState<string>("b92785ff-7eae-4f47-aa16-1cda87771b0d");

  // paging
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  // data
  const [paged, setPaged] = useState<PageActiveSessionDetailsDTO | null>(null);
  const [rows, setRows] = useState<ActiveSessionDetailsDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  // client filter
  const [q, setQ] = useState("");

  // auto-refresh
  const [auto, setAuto] = useState(true);
  const timerRef = useRef<number | null>(null);

  const filtered = useMemo(() => {
    const n = q.trim().toLowerCase();
    if (!n) return rows;
    return rows.filter((r) => {
      const parts = [
        r.driver?.fullName,
        r.driver?.phone,
        r.driver?.id,
        r.sessionId,
        r.averageEmotion,
        r.unresolvedAlertsCount?.toString(),
      ]
        .filter(Boolean)
        .map(String)
        .join(" ")
        .toLowerCase();
      return parts.includes(n);
    });
  }, [rows, q]);

  function pickLatestAlert(alerts?: AlertDTO[]): AlertDTO | undefined {
    if (!alerts || alerts.length === 0) return undefined;
    return [...alerts].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )[0];
  }

  async function load() {
    setLoading(true);
    setErr(null);
    try {
      const pg = await getActiveSessionsWithDetailsPaged(
        companyId || undefined,
        page,
        size,
        ["startTime,desc"]
      );
      setPaged(pg);
      setRows(pg.content ?? []);
    } catch (e: any) {
      setErr(e?.message ?? "Failed to load active sessions");
      setPaged(null);
      setRows([]);
    } finally {
      setLoading(false);
    }
  }

  // initial + when params change
  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [companyId, page, size]);

  // auto refresh every 5s
  useEffect(() => {
    if (!auto) return;
    const tick = () => load();
    tick();
    timerRef.current = window.setInterval(tick, 5000);
    return () => {
      if (timerRef.current) window.clearInterval(timerRef.current);
      timerRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auto, companyId, page, size]);

  return (
    <div className="card">
      {/* Header */}
      <div className="card-header d-flex align-items-center gap-2 flex-wrap">
        <strong>Active Sessions</strong>

        <div className="ms-auto d-flex align-items-center gap-2 flex-wrap">
          {/* optional company scope */}
          <input
            type="text"
            className="form-control form-control-sm"
            placeholder="Company ID (optional)"
            value={companyId}
            onChange={(e) => {
              setCompanyId(e.target.value.trim());
              setPage(0);
            }}
            style={{ width: 250 }}
          />

          <input
            type="search"
            className="form-control form-control-sm"
            placeholder="Filter name / phone / ids"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            style={{ width: 240 }}
          />

          <button
            className={`btn btn-sm ${auto ? "btn-outline-danger" : "btn-outline-success"}`}
            onClick={() => setAuto((v) => !v)}
            title={auto ? "Pause auto-refresh" : "Resume auto-refresh"}
          >
            {auto ? "Pause" : "Auto"}
          </button>

          <button
            className="btn btn-sm btn-outline-secondary"
            onClick={load}
            disabled={loading}
          >
            {loading ? "Refreshing…" : "Refresh"}
          </button>
        </div>
      </div>

      {/* Body */}
      <div className="card-body">
        {err && <div className="alert alert-danger">{err}</div>}
        {!err && loading && rows.length === 0 && <div>Loading…</div>}
        {!loading && filtered.length === 0 && !err && (
          <div className="text-muted">No active sessions.</div>
        )}

        {filtered.length > 0 && (
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead className="table-light">
                <tr>
                  <th style={{ width: "30%" }}>Driver</th>
                  <th style={{ width: "16%" }}>Started</th>
                  <th style={{ width: "12%" }}>Elapsed</th>
                  <th style={{ width: "18%" }}>Alerts</th>
                  <th style={{ width: "14%" }}>Avg Emotion</th>
                  <th style={{ width: "10%" }} className="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((r) => {
                  const unresolved = r.unresolvedAlertsCount ?? 0;
                  const latest = pickLatestAlert(r.alerts);
                  const rowDanger = unresolved > 0;

                  return (
                    <tr key={r.sessionId} className={rowDanger ? "table-danger" : ""}>
                      <td>
                        <div className="fw-semibold">
                          {r.driver?.fullName || "—"}
                        </div>
                        <div className="text-muted small">
                          {r.driver?.phone || "—"}
                          {r.driver?.id ? ` · ${String(r.driver.id).slice(0, 8)}…` : ""}
                          {` · Session: ${r.sessionId.slice(0, 8)}…`}
                        </div>
                      </td>

                      <td>{fmtDT(r.startTime)}</td>
                      <td>{fmtElapsedFrom(r.startTime)}</td>

                      <td>
                        {unresolved > 0 ? (
                          <span className="badge text-bg-danger me-2">
                            {unresolved} unresolved
                          </span>
                        ) : (
                          <span className="badge text-bg-success me-2">OK</span>
                        )}
                        <span className="text-muted small d-block mt-1">
                          {latest
                            ? <>Last: <strong>{nice(latest.type)}</strong> · {fmtDT(latest.createdAt)}</>
                            : "No recent alerts"}
                        </span>
                      </td>

                      <td className="text-uppercase text-muted">
                        {r.averageEmotion || "—"}
                      </td>

                      <td className="text-end">
                        <div className="btn-group">
                         
                          <Link
                            className={`btn btn-sm ${unresolved > 0 ? "btn-danger" : "btn-outline-success"}`}
                            to={`/admin/sessions/${r.sessionId}/live`}
                            title="Start live surveillance"
                          >
                            Surveil
                          </Link>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {paged && paged.totalPages > 1 && (
          <div className="d-flex align-items-center justify-content-between mt-2">
            <div className="text-muted small">
              Page {paged.number + 1} / {paged.totalPages} · {paged.totalElements} total
            </div>
            <div className="d-flex align-items-center gap-2">
              <button className="btn btn-outline-secondary btn-sm" disabled={paged.first} onClick={() => setPage(0)}>
                « First
              </button>
              <button className="btn btn-outline-secondary btn-sm" disabled={paged.first} onClick={() => setPage((p) => Math.max(0, p - 1))}>
                ‹ Prev
              </button>
              <button className="btn btn-outline-secondary btn-sm" disabled={paged.last} onClick={() => setPage((p) => p + 1)}>
                Next ›
              </button>
              <button className="btn btn-outline-secondary btn-sm" disabled={paged.last} onClick={() => setPage(paged.totalPages - 1)}>
                Last »
              </button>

              <select
                className="form-select form-select-sm"
                style={{ width: 90 }}
                value={size}
                onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}
              >
                {[10, 20, 50].map((n) => (
                  <option key={n} value={n}>{n}/page</option>
                ))}
              </select>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
