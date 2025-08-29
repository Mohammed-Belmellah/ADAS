// src/pages/auth/RegisterIndividualDriver.tsx
import { useState } from "react";
import { registerIndividualDriver } from "../../../api/registration";
import { loginWithHint } from "../../../auth/keycloak";

export default function RegisterIndividualDriver() {
  const [name, setName] = useState(""); 
  const [email, setEmail] = useState(""); 
  const [password, setPassword] = useState(""); 
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr(null); setBusy(true);
    try {
      await registerIndividualDriver({ name, email, password });
      loginWithHint(email);
    } catch (e: any) {
      setErr(e?.response?.data?.error || "Registration failed");
    } finally { setBusy(false); }
  }

  return (
    <div className="container py-5" style={{maxWidth: 560}}>
      <h3>Register as Independent Driver</h3>
      <form className="mt-4" onSubmit={submit}>
        <div className="mb-3"><label className="form-label">Your name</label>
          <input className="form-control" value={name} onChange={e=>setName(e.target.value)} required />
        </div>
        <div className="mb-3"><label className="form-label">Email</label>
          <input type="email" className="form-control" value={email} onChange={e=>setEmail(e.target.value)} required />
        </div>
        <div className="mb-3"><label className="form-label">Password</label>
          <input type="password" className="form-control" value={password} onChange={e=>setPassword(e.target.value)} minLength={8} required />
        </div>
        {err && <div className="alert alert-danger">{err}</div>}
        <button className="btn btn-primary" disabled={busy}>Create account</button>
      </form>
    </div>
  );
}
