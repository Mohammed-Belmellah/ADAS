import CardButton from "../../components/common/CardButton";
import { countActiveSessions } from "../../api/sessions";
import { countCompanyDrivers } from "../../api/drivers";

export default function Dashboard() {
  return (
    <>
      <h2 className="mb-4">Admin Dashboard</h2>

      <div className="row g-3">
        <div className="col-md-6">
          <CardButton
            title="Active Sessions"
            to="/admin/active-sessions"
            fetchCount={countActiveSessions}
            subtitle="View ongoing sessions and start live surveillance."
          />
        </div>

        <div className="col-md-6">
          <CardButton
            title="Company Drivers"
            to="/admin/drivers"
            fetchCount={countCompanyDrivers}
            subtitle="Browse all company drivers and open profiles."
          />
        </div>
      </div>
    </>
  );
}
