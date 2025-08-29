import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiGet } from "../../api/client"; // <-- use the wrapper

type CompanyDriver = {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  companyId?: string;
};

export default function DriversPage() {
  const [drivers, setDrivers] = useState<CompanyDriver[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    let alive = true;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await apiGet<CompanyDriver[]>("/api/company-drivers");
        if (!alive) return;
        setDrivers(data);
      } catch (e: any) {
        if (!alive) return;
        setError(e?.message || "Failed to load drivers");
        setDrivers([]);
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, []);

  return (
    <div>
      <h3 className="mb-3">Company Drivers</h3>

      {loading && <div>Loading…</div>}

      {!loading && error && (
        <div className="alert alert-danger">
          {error}
          {/* common cases */}
          {/^401/.test(error) && <div className="small mt-1">Please sign in again.</div>}
          {/^403/.test(error) && <div className="small mt-1">You don't have access to this list.</div>}
        </div>
      )}

      {!loading && !error && drivers && drivers.length === 0 && (
        <div className="alert alert-info">No drivers found.</div>
      )}

      {!loading && !error && drivers && drivers.length > 0 && (
        <div className="list-group">
          {drivers.map((d) => (
            <button
              key={d.id}
              className="list-group-item list-group-item-action text-start"
              onDoubleClick={() => navigate(`/admin/drivers/${d.id}`)}
              title="Double-click to open profile"
            >
              <div className="fw-semibold">{d.name ?? "Unnamed driver"}</div>
              <div className="text-muted small">
                {d.email ?? "—"} {d.phone ? ` • ${d.phone}` : ""}
              </div>
            </button>
          ))}
        </div>
      )}

      <div className="form-text mt-2">Double-click a driver to open profile.</div>
    </div>
  );
}
