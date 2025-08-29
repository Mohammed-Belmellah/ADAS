// src/pages/admin/DriverDetailPage.tsx
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import DriverPerformancePanel from "../../components/drivers/DriverPerformancePanel";
import DriverSessionsSearch from "../../components/drivers/DriverSessionsSearch";
import { apiGet } from "../../api/client";               // <-- use wrapper
import { hasSessionReport } from "../../api/sessions";   // make sure this uses apiGet too

/** ---- Backend DTOs (subset) ---- */
type CompanyDriverResponseDTO = {
  id: string; name?: string; email?: string; phone?: string;
  companyId?: string; licenseNumber?: string; vehicleId?: string;
};
type IndividualDriverResponseDTO = {
  id: string; name?: string; email?: string; phone?: string;
  licenseNumber?: string; vehicleId?: string;
};
type UserSummary = { role?: "ADMIN" | "DRIVER_COMPANY" | "DRIVER_INDEPENDENT" };

type NormalizedDriver = {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  companyId?: string;         // only for company drivers
  licenseNumber?: string;
  vehicleId?: string;
  kind: "company" | "individual";
};

type SessionSummary = {
  sessionId: string;
  startTime: string;
  endTime?: string | null;
  dominantEmotion?: string | null;
  safetyEmotionScore?: number | null;
};

/** Decide endpoint by role (no probing), then fetch once */
async function fetchDriverNormalized(id: string): Promise<NormalizedDriver> {
  const u = await apiGet<UserSummary>(`/api/users/${id}`);
  if (u.role === "DRIVER_INDEPENDENT") {
    const d = await apiGet<IndividualDriverResponseDTO>(`/api/individual-drivers/${id}`);
    return {
      id: d.id, name: d.name ?? "Driver", email: d.email, phone: d.phone,
      licenseNumber: d.licenseNumber, vehicleId: d.vehicleId, kind: "individual",
    };
  } else {
    const d = await apiGet<CompanyDriverResponseDTO>(`/api/company-drivers/${id}`);
    return {
      id: d.id, name: d.name ?? "Driver", email: d.email, phone: d.phone,
      companyId: d.companyId, licenseNumber: d.licenseNumber,
      vehicleId: d.vehicleId, kind: "company",
    };
  }
}

export default function DriverDetailPage() {
  const navigate = useNavigate();
  const { driverId } = useParams();
  const [driver, setDriver] = useState<NormalizedDriver | null>(null);
  const [recent, setRecent] = useState<SessionSummary[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reportStatus, setReportStatus] = useState<Record<string, boolean>>({});

  useEffect(() => {
    if (!driverId) return;
    let alive = true;

    (async () => {
      try {
        setLoading(true);
        setError(null);

        // 1) fetch driver (role-aware)
        const normalized = await fetchDriverNormalized(driverId);

        // 2) fetch recent sessions (secured)
        const r = await apiGet<SessionSummary[]>(
          `/api/sessions/drivers/${driverId}/recent?limit=5`
        );

        if (!alive) return;
        setDriver(normalized);
        setRecent(r);

        // 3) pre-check report availability for ended recent sessions
        const status: Record<string, boolean> = {};
        await Promise.all(
          r.map(async (s) => {
            if (s.endTime) status[s.sessionId] = await hasSessionReport(s.sessionId);
          })
        );
        if (!alive) return;
        setReportStatus(status);
      } catch (e: any) {
        if (!alive) return;
        setError(e?.message ?? "Failed to load driver details");
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => { alive = false; };
  }, [driverId]);

  if (loading) return <div>Loading driver…</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!driver) return <div className="alert alert-warning">Driver not found.</div>;

  return (
    <div>
      <div className="d-flex align-items-center gap-2 mb-2">
        <h3 className="mb-0">{driver.name ?? "Driver"}</h3>
        <span className={`badge ${driver.kind === "company" ? "text-bg-primary" : "text-bg-secondary"}`}>
          {driver.kind === "company" ? "Company Driver" : "Individual Driver"}
        </span>
      </div>

      {/* Driver info */}
      <div className="card mb-4">
        <div className="card-body row g-3">
          <Info label="Email" value={driver.email ?? "—"} />
          <Info label="Phone" value={driver.phone ?? "—"} />
          {driver.companyId && <Info label="Company ID" value={driver.companyId} />}
          <Info label="License #" value={driver.licenseNumber ?? "—"} />
          <Info label="Vehicle ID" value={driver.vehicleId ?? "—"} />
        </div>
      </div>

      <div className="mb-4">
        <DriverPerformancePanel driverId={driver.id} />
      </div>

      <div className="mb-4">
        <DriverSessionsSearch driverId={driver.id} />
      </div>

      <h5 className="mb-2">Recent Sessions</h5>
      {!recent ? (
        <div>Loading recent…</div>
      ) : recent.length === 0 ? (
        <div className="alert alert-info">No recent sessions.</div>
      ) : (
        <div className="list-group">
          {recent.map((s) => {
            const ended = !!s.endTime;
            const hasReport = reportStatus[s.sessionId] === true;
            return (
              <div key={s.sessionId} className="list-group-item d-flex justify-content-between align-items-center">
                <div>
                  <div className="fw-semibold">Session {s.sessionId.slice(0, 8)}…</div>
                  <div className="text-muted small">
                    {new Date(s.startTime).toLocaleString()} —{" "}
                    {ended ? new Date(s.endTime!).toLocaleString() : "ongoing"}
                    {s.dominantEmotion ? ` • Dominant: ${s.dominantEmotion}` : ""}
                    {s.safetyEmotionScore != null ? ` • Safety: ${s.safetyEmotionScore}` : ""}
                  </div>
                </div>
                <div className="d-flex align-items-center gap-2">
                  {!ended && <span className="badge text-bg-warning">Ongoing</span>}
                  {ended && !hasReport && <span className="badge text-bg-secondary">No Report</span>}
                  {ended && hasReport && (
                    <button
                      className="btn btn-outline-secondary"
                      onClick={() => navigate(`/admin/sessions/${s.sessionId}/report`)}
                    >
                      View report
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div className="col-md-4">
      <div className="text-muted small">{label}</div>
      <div className="fw-semibold">{value}</div>
    </div>
  );
}
