// src/auth/keycloak.ts
import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KC_URL,
  realm: import.meta.env.VITE_KC_REALM,
  clientId: import.meta.env.VITE_KC_CLIENT,
});

/**
 * Initialize KC. Public routes (e.g. /register/**) use check-sso so the page loads
 * without forcing login. Everything else uses login-required.
 */
export async function initAuth() {
  const publicPaths = [/^\/register(\/|$)/, /^\/public(\/|$)/];
  const isPublic = publicPaths.some(rx => rx.test(location.pathname));

  await keycloak.init({
    onLoad: isPublic ? "check-sso" : "login-required",
    pkceMethod: "S256",
    // Optional (for silent SSO on public pages):
    // silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
  });
}

/** Optional helper used after successful signup to prefill the login screen */
export function loginWithHint(email?: string) {
  return keycloak.login({ loginHint: email });
}

export async function getAccessToken() {
  if (keycloak.isTokenExpired(30)) await keycloak.updateToken(30);
  return keycloak.token ?? "";
}

type TokenParsed = {
  realm_access?: { roles?: string[] };
  userId?: string | null;
  companyId?: string | null;
  [k: string]: any;
};

const parsed = (): TokenParsed => (keycloak.tokenParsed || {}) as TokenParsed;

export const roles = () => (parsed().realm_access?.roles ?? []).map(r => r.toUpperCase());
export const hasRole = (r: "ADMIN" | "DRIVER" | "DRIVER_COMPANY" | "DRIVER_INDEPENDENT") =>
  roles().includes(r.toUpperCase());
export const hasAnyRole = (...rs: string[]) => rs.some(r => roles().includes(r.toUpperCase()));

export const claim = <T = any>(name: keyof TokenParsed | string): T | null =>
  ((parsed() as any)[name] ?? null) as T | null;

export const me = {
  /** Canonical id for EVERY user (admins & drivers) */
  userId:   () => parsed().userId ?? null,
  /** Present for admins & company drivers */
  companyId:() => parsed().companyId ?? null,
};

export const isAdmin  = () => hasRole("ADMIN");
export const isDriver = () => hasAnyRole("DRIVER","DRIVER_COMPANY","DRIVER_INDEPENDENT");
