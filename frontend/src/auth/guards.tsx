import { Navigate, useParams } from "react-router-dom";
import { useEffect, useState, type ReactNode } from "react";
import { hasRole, me } from "./keycloak";
import { apiGet } from "../api/client";

/** Gate by role (you grant ADMIN and generic DRIVER) */
export function RequireRole({
  allow,
  children,
}: {
  allow: Array<"ADMIN" | "DRIVER">;
  children: ReactNode;
}) {
  return allow.some(hasRole) ? <>{children}</> : <Navigate to="/admin" replace />;
}

/** Allow ADMIN, or DRIVER viewing their own :driverId (now it's userId) */
export function RequireSelfDriverOrAdmin({ children }: { children: ReactNode }) {
  const { driverId } = useParams();            // route still named :driverId; it contains userId
  const isAdmin = hasRole("ADMIN");
  const self = me.userId();

  if (isAdmin) return <>{children}</>;
  if (!driverId) return hasRole("DRIVER") ? <>{children}</> : <Navigate to="/admin" replace />;

  return hasRole("DRIVER") && self && self.toLowerCase() === driverId.toLowerCase()
    ? <>{children}</>
    : <Navigate to="/admin" replace />;
}

/**
 * Block ADMIN on individual drivers.
 * Uses /api/users/{id} which returns BaseUserResponseDTO with role.
 */
export function BlockAdminOnIndividual({ children }: { children: ReactNode }) {
  const { driverId } = useParams(); // userId value in URL
  const isAdmin = hasRole("ADMIN");
  const [ok, setOk] = useState<boolean | null>(isAdmin ? null : true);

  useEffect(() => {
    if (!isAdmin) return;
    if (!driverId) { setOk(false); return; }

    let stop = false;
    (async () => {
      try {
        const u = await apiGet<{ role?: string }>(`/api/users/${driverId}`);
        if (!stop) setOk(u.role !== "DRIVER_INDEPENDENT");
      } catch {
        if (!stop) setOk(false);
      }
    })();

    return () => { stop = true; };
  }, [driverId, isAdmin]);

  if (ok === null) return <div className="p-3">Loading…</div>;
  return ok ? <>{children}</> : <Navigate to="/admin" replace />;
}
