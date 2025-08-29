// src/App.tsx
import { Routes, Route, Navigate } from "react-router-dom";
import AdminLayout from "./app/layouts/AdminLayout";

import ActiveSessionsPage from "./pages/admin/ActiveSessionsPage";
import DriversPage from "./pages/admin/DriversPage";
import DriverDetailPage from "./pages/admin/DriverDetailPage";
import SessionReportPage from "./pages/admin/SessionReportPage";
import LiveSessionPage from "./pages/admin/LiveSessionPage";
import AutoHome from "./pages/admin/AutoHome";
import { RequireRole, RequireSelfDriverOrAdmin, BlockAdminOnIndividual } from "./auth/guards";

// public self-registration pages
import RegisterChoice from "./pages/admin/auth/RegisterChoice";
import RegisterCompanyAdmin from "./pages/admin/auth/RegisterCompanyAdmin";
import RegiterCompanyDriver from "./pages/admin/auth/RegiterCompanyDriver";
import RegisterIndividualDriver from "./pages/admin/auth/RegisterIndividualDriver";

// forbidden page
import Forbiden from "./pages/Forbiden";

export default function App() {
  return (
    <Routes>
      {/* public self-registration */}
      <Route path="/register" element={<RegisterChoice />} />
      <Route path="/register/company-admin" element={<RegisterCompanyAdmin />} />
      <Route path="/register/company-driver" element={<RegiterCompanyDriver />} />
      <Route path="/register/individual-driver" element={<RegisterIndividualDriver />} />

      {/* protected area */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<RequireRole allow={["ADMIN", "DRIVER"]}><AutoHome/></RequireRole>} />
        <Route path="active-sessions" element={<RequireRole allow={["ADMIN", "DRIVER"]}><ActiveSessionsPage/></RequireRole>} />
        <Route path="sessions/:sessionId/live" element={<RequireRole allow={["ADMIN"]}><LiveSessionPage/></RequireRole>} />
        <Route path="sessions/:sessionId/report" element={<RequireSelfDriverOrAdmin><SessionReportPage/></RequireSelfDriverOrAdmin>} />
        <Route path="drivers" element={<RequireRole allow={["ADMIN"]}><DriversPage/></RequireRole>} />
        <Route
          path="drivers/:driverId"
          element={
            <RequireSelfDriverOrAdmin>
              <BlockAdminOnIndividual>
                <DriverDetailPage/>
              </BlockAdminOnIndividual>
            </RequireSelfDriverOrAdmin>
          }
        />
      </Route>

      <Route path="/forbidden" element={<Forbiden />} />
      <Route path="/" element={<Navigate to="/admin" replace />} />
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  );
}
