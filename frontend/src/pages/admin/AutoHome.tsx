import { Navigate } from "react-router-dom";
import Dashboard from "./Dashboard";
import { isAdmin, isDriver, me, keycloak } from "../../auth/keycloak";

export default function AutoHome() {
  // optional: avoid flicker before KC init
  if (keycloak.authenticated === undefined) {
    return <div className="p-3">Loading…</div>;
  }

  if (isAdmin()) return <Dashboard />;

  if (isDriver()) {
    const id = me.userId();
    if (id) return <Navigate to={`/admin/drivers/${id}`} replace />;
  }

  return <Dashboard />;
}
