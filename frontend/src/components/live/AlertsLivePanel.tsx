import { useMemo } from "react";

export type LiveAlert = {
  id?: string;
  sessionId?: string;
  type: string;
  message?: string;
  createdAt?: string; // ISO
  resolved?: boolean;
};

const nice = (s: string) =>
  s.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());

export default function AlertsLivePanel({
  counts,
  recent,
}: {
  counts: Record<string, number>;
  recent: LiveAlert[];
}) {
  const total = useMemo(() => Object.values(counts).reduce((a, b) => a + b, 0), [counts]);
  const sorted = useMemo(
    () =>
      Object.entries(counts)
        .map(([type, count]) => ({ type, count }))
        .sort((a, b) => b.count - a.count),
    [counts]
  );

  return (
    <div className="card h-100">
      <div className="card-header d-flex align-items-center justify-content-between">
        <span>Alerts (live)</span>
        <span className="badge text-bg-secondary">{total} total</span>
      </div>
      <div className="card-body">
        {sorted.length === 0 ? (
          <div className="text-muted">No alerts yet.</div>
        ) : (
          <>
            <ul className="list-group list-group-flush mb-3">
              {sorted.map((r) => (
                <li key={r.type} className="list-group-item d-flex justify-content-between">
                  <span>{nice(r.type)}</span>
                  <span className="badge text-bg-secondary">{r.count}</span>
                </li>
              ))}
            </ul>

            <details>
              <summary>Recent alerts</summary>
              <ul className="list-group list-group-flush mt-2">
                {recent.map((a, i) => (
                  <li key={(a.id ?? i) + "-" + (a.createdAt ?? i)} className="list-group-item">
                    <div className="d-flex justify-content-between">
                      <strong>{nice(a.type)}</strong>
                      <span className="text-muted small">
                        {a.createdAt ? new Date(a.createdAt).toLocaleTimeString() : ""}
                      </span>
                    </div>
                    {a.message && <div className="text-muted small">{a.message}</div>}
                  </li>
                ))}
              </ul>
            </details>
          </>
        )}
      </div>
    </div>
  );
}
