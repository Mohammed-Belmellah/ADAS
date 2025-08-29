// src/components/drivers/DriverSessionsSearch.tsx
import { useEffect, useMemo, useState } from "react";
import {
  type SessionDTO,
  type Page as PageType,
  getSessionsByDate,
  getSessionsByRange,
  getSessionsByStartBetween,
  hasSessionReport,
} from "../../api/sessions";
import { useNavigate } from "react-router-dom";

type Props = { driverId: string };

function fmtDT(iso?: string | null) {
  return iso ? new Date(iso).toLocaleString() : "—";
}
function fmtDur(start?: string, end?: string | null) {
  if (!start || !end) return "—";
  const s = new Date(start).getTime();
  const e = new Date(end).getTime();
  const sec = Math.max(0, Math.round((e - s) / 1000));
  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  return h ? `${h}h ${m}m` : `${m}m`;
}
function toISO(local: string) {
  return local ? new Date(local).toISOString() : "";
}
const hasEnded = (s: SessionDTO) => !!s.endTime;

export default function DriverSessionsSearch({ driverId }: Props) {
  const navigate = useNavigate(); // ✅ inside the component

  type Mode = "date" | "range" | "datetime";
  const [mode, setMode] = useState<Mode>("range");

  // Inputs
  const [date, setDate] = useState("");        // YYYY-MM-DD
  const [from, setFrom] = useState("");        // YYYY-MM-DD
  const [to, setTo] = useState("");            // YYYY-MM-DD
  const [startDT, setStartDT] = useState("");  // YYYY-MM-DDTHH:mm
  const [endDT, setEndDT] = useState("");      // YYYY-MM-DDTHH:mm
  const [zone, setZone] = useState("");        // optional IANA TZ (date/range only)

  // Paging
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const [rows, setRows] = useState<SessionDTO[]>([]);
  const [paged, setPaged] = useState<PageType<SessionDTO> | null>(null);

  // Which sessions have reports
  const [reportsMap, setReportsMap] = useState<Record<string, boolean>>({});

  const canSearch = useMemo(() => {
    if (!driverId) return false;
    if (mode === "date") return !!date;
    if (mode === "range") return !!from && !!to && new Date(from) <= new Date(to);
    if (mode === "datetime") return !!startDT && !!endDT && new Date(startDT) <= new Date(endDT);
    return false;
  }, [driverId, mode, date, from, to, startDT, endDT]);

  async function search() {
    if (!canSearch) return;
    setLoading(true);
    setErr(null);
    try {
      let list: SessionDTO[] = [];
      let pg: PageType<SessionDTO> | null = null;

      if (mode === "date") {
        list = await getSessionsByDate(driverId, date, zone || undefined);
      } else if (mode === "range") {
        pg = await getSessionsByRange(
          driverId, from, to, page, size, zone || undefined, ["startTime,desc"]
        );
        list = pg.content;
      } else {
        const pgDT = await getSessionsByStartBetween(
          driverId, toISO(startDT), toISO(endDT), page, size, ["startTime,desc"]
        );
        pg = pgDT;
        list = pgDT.content;
      }

      setRows(list);
      setPaged(pg);

      // Pre-check report existence (only for ended sessions)
      const statuses: Record<string, boolean> = {};
      await Promise.all(
        list.map(async (s) => {
          if (hasEnded(s)) {
            statuses[s.id] = await hasSessionReport(s.id);
          }
        })
      );
      setReportsMap(statuses);
    } catch (e: any) {
      setErr(e?.message ?? "Search failed");
      setRows([]);
      setPaged(null);
      setReportsMap({});
    } finally {
      setLoading(false);
    }
  }

  // Auto-search when paging changes (for paged modes)
  useEffect(() => {
    if (mode === "range" && from && to) search();
    if (mode === "datetime" && startDT && endDT) search();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);

  // Reset page when inputs/mode change
  useEffect(() => { setPage(0); }, [mode, from, to, startDT, endDT]);

  return (
    <div className="card">
      {/* HEADER / SEARCH BAR */}
      <div className="card-header d-flex align-items-center gap-3 flex-wrap">
        <strong>Search Sessions</strong>

        <div className="ms-auto d-flex align-items-center gap-2 flex-wrap">
          <select
            className="form-select form-select-sm"
            style={{ width: 180 }}
            value={mode}
            onChange={(e) => setMode(e.target.value as Mode)}
          >
            <option value="range">By Range (date)</option>
            <option value="date">By Date</option>
            <option value="datetime">By Start–End (time)</option>
          </select>

          {mode === "date" && (
            <>
              <input
                type="date"
                className="form-control form-control-sm"
                style={{ width: 160 }}
                value={date}
                onChange={(e) => setDate(e.target.value)}
              />
              <input
                type="text"
                className="form-control form-control-sm"
                placeholder="Timezone (opt.)"
                style={{ width: 180 }}
                value={zone}
                onChange={(e) => setZone(e.target.value)}
              />
            </>
          )}

          {mode === "range" && (
            <>
              <input
                type="date"
                className="form-control form-control-sm"
                style={{ width: 160 }}
                value={from}
                onChange={(e) => setFrom(e.target.value)}
              />
              <span>to</span>
              <input
                type="date"
                className="form-control form-control-sm"
                style={{ width: 160 }}
                value={to}
                onChange={(e) => setTo(e.target.value)}
              />
              <input
                type="text"
                className="form-control form-control-sm"
                placeholder="Timezone (opt.)"
                style={{ width: 180 }}
                value={zone}
                onChange={(e) => setZone(e.target.value)}
              />
            </>
          )}

          {mode === "datetime" && (
            <>
              <input
                type="datetime-local"
                className="form-control form-control-sm"
                style={{ width: 220 }}
                value={startDT}
                onChange={(e) => setStartDT(e.target.value)}
              />
              <span>to</span>
              <input
                type="datetime-local"
                className="form-control form-control-sm"
                style={{ width: 220 }}
                value={endDT}
                onChange={(e) => setEndDT(e.target.value)}
              />
            </>
          )}

          <button
            className="btn btn-sm btn-primary"
            onClick={search}
            disabled={!canSearch || loading}
          >
            {loading ? "Searching…" : "Search"}
          </button>
        </div>
      </div>

      {/* BODY / RESULTS */}
      <div className="card-body">
        {err && <div className="alert alert-danger">{err}</div>}
        {!err && loading && <div>Loading…</div>}
        {!loading && rows.length === 0 && !err && <div className="text-muted">No sessions.</div>}

        {rows.length > 0 && (
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead className="table-light">
                <tr>
                  <th style={{ width: "22%" }}>Start</th>
                  <th style={{ width: "22%" }}>End</th>
                  <th style={{ width: "14%" }}>Duration</th>
                  <th style={{ width: "18%" }}>Avg Emotion</th>
                  <th style={{ width: "24%" }} className="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((s) => (
                  <tr key={s.id}>
                    <td>{fmtDT(s.startTime)}</td>
                    <td>{fmtDT(s.endTime)}</td>
                    <td>{fmtDur(s.startTime, s.endTime)}</td>
                    <td className="text-uppercase text-muted">{s.averageEmotion ?? "—"}</td>
                    <td className="text-end">
                      {hasEnded(s) ? (
                        reportsMap[s.id] ? (
                          <button
                            className="btn btn-sm btn-outline-secondary me-2"
                            onClick={() => navigate(`/admin/sessions/${s.id}/report`)}
                          >
                            View Report
                          </button>
                        ) : (
                          <span className="badge text-bg-secondary me-2">No Report</span>
                        )
                      ) : (
                        <span className="badge text-bg-warning me-2">Ongoing</span>
                      )}

                      <a className="btn btn-sm btn-outline-primary" href={`/admin/sessions/${s.id}`}>
                        Open Session
                      </a>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {(mode === "range" || mode === "datetime") && paged && paged.totalPages > 1 && (
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
