// src/pages/auth/RegisterChoice.tsx
import { Link } from "react-router-dom";

export default function RegisterChoice() {
  return (
    <div className="container py-5">
      <h2>Create an account</h2>
      <p className="text-muted">Choose what best describes you.</p>
      <div className="d-flex gap-3 mt-4">
        <Link className="btn btn-primary" to="/register/company-admin">I’m a Company Admin</Link>
        <Link className="btn btn-outline-primary" to="/register/company-driver">I’m a Company Driver</Link>
        <Link className="btn btn-outline-secondary" to="/register/individual-driver">I’m an Independent Driver</Link>
      </div>
    </div>
  );
}
