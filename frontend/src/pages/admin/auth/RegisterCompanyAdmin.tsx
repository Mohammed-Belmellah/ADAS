// src/pages/auth/RegisterCompanyAdmin.tsx
import { useState } from "react";
import { registerCompanyAdmin } from "../../../api/registration";
import { loginWithHint } from "../../../auth/loginWithHint";// or from loginWithHint helper if separate

export default function RegisterCompanyAdmin() {
  const [name, setName] = useState("");
  const [companyName, setCompanyName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr(null);
    setBusy(true);
    try {
      await registerCompanyAdmin({ name, email, password, companyName });
      loginWithHint(email); // redirect to Keycloak with email prefilled
    } catch (e: any) {
      setErr(e?.message || "Registration failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="container py-5" style={{ maxWidth: 560 }}>
      <h3>Register as Company Admin</h3>
      <form className="mt-4" onSubmit={submit}>
        <div className="mb-3">
          <label className="form-label">Your name</label>
          <input className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div className="mb-3">
          <label className="form-label">Company name</label>
          <input className="form-control" value={companyName} onChange={(e) => setCompanyName(e.target.value)} required />
        </div>
        <div className="mb-3">
          <label className="form-label">Email</label>
          <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div className="mb-3">
          <label className="form-label">Password</label>
          <input type="password" className="form-control" minLength={8} value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        {err && <div className="alert alert-danger">{err}</div>}
        <button className="btn btn-primary" disabled={busy}>Create account</button>
      </form>
    </div>
  );
}
