import { Outlet, NavLink } from "react-router-dom";
import { isAdmin, isDriver, me, keycloak } from "../../auth/keycloak";

export default function AdminLayout() {
  const myId = me.userId();

  const Link = ({ to, label }: { to: string; label: string }) => (
    <NavLink to={to} end className={({isActive}) => "nav-link text-white" + (isActive?" active fw-semibold":"")}>
      {label}
    </NavLink>
  );

  return (
    <div className="d-flex" style={{ minHeight: "100vh" }}>
      <aside className="bg-dark text-white p-3 d-flex flex-column" style={{ width: 260 }}>
        <div className="d-flex align-items-center justify-content-between mb-4">
          <h4 className="mb-0">ADAS</h4>
          <button className="btn btn-sm btn-outline-light" onClick={() => keycloak.logout()}>Logout</button>
        </div>

        <nav className="nav flex-column gap-1">
          <Link to="/admin" label="Dashboard" />
          <Link to="/admin/active-sessions" label="Active Sessions" />

          {isAdmin() ? (
            <>
              <span className="text-white-50 small mt-3">Drivers</span>
              <Link to="/admin/drivers" label="All Drivers" />
            </>
          ) : (
            isDriver() && myId && (
              <>
                <span className="text-white-50 small mt-3">My Area</span>
                <Link to={`/admin/drivers/${myId}`} label="My Driver Page" />
              </>
            )
          )}
        </nav>
      </aside>

      <main className="flex-grow-1 p-4 bg-light">
        <Outlet />
      </main>
    </div>
  );
}
