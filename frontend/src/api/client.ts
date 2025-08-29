// src/api/client.ts
import { keycloak } from "../auth/keycloak";

const RAW_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";
const BASE = RAW_BASE.replace(/\/+$/, ""); // strip trailing slash

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

/** Endpoints that must work without auth */
const PUBLIC_PATHS = [/^\/auth\/register\//, /^\/public\//, /^\/actuator\/health$/];
const isPublicPath = (path: string) => PUBLIC_PATHS.some((rx) => rx.test(path));

/** Safe bearer getter: returns "" if not authenticated */
async function getBearerIfAny(): Promise<string> {
  // Don’t try to refresh or login if we’re not authenticated yet
  if (!keycloak.authenticated || !keycloak.token) return "";
  try {
    if (keycloak.isTokenExpired(30)) await keycloak.updateToken(30);
  } catch {
    // Refresh failed → treat as anonymous for this request
    return "";
  }
  return keycloak.token ?? "";
}

async function doFetch<T>(
  method: HttpMethod,
  path: string,
  body?: unknown,
  init?: RequestInit,
  retry = true
): Promise<T> {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const url = `${BASE}${normalizedPath}`;
  const publicCall = isPublicPath(normalizedPath);

  // Only add/refresh token for protected calls
  const token = publicCall ? "" : await getBearerIfAny();

  const res = await fetch(url, {
    ...init,
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers || {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // Retry ONCE on 401 for protected calls when we had a token
  if (res.status === 401 && retry && !publicCall && token) {
    try {
      await keycloak.updateToken(0); // force refresh
    } catch {
      keycloak.login(); // fall back to login on protected views
      throw new Error("401 Unauthorized");
    }
    return doFetch<T>(method, path, body, init, false);
  }

  if (!res.ok) {
    // Prefer JSON message if available
    let message = res.statusText;
    try {
      const ct = res.headers.get("content-type") || "";
      if (ct.includes("application/json")) {
        const data: any = await res.json();
        message = data?.message || data?.error || JSON.stringify(data);
      } else {
        message = (await res.text()) || message;
      }
    } catch {
      /* ignore parse errors */
    }
    throw new Error(`${res.status}: ${message}`);
  }

  if (res.status === 204) return undefined as unknown as T;
  return (await res.json()) as T;
}

/* ---------- Convenience wrappers ---------- */

export const apiGet = <T>(path: string, init?: RequestInit) =>
  doFetch<T>("GET", path, undefined, init);

export const apiPost = <T>(path: string, body?: unknown, init?: RequestInit) =>
  doFetch<T>("POST", path, body, init);

export const apiPut = <T>(path: string, body?: unknown, init?: RequestInit) =>
  doFetch<T>("PUT", path, body, init);

export const apiPatch = <T>(path: string, body?: unknown, init?: RequestInit) =>
  doFetch<T>("PATCH", path, body, init);

export const apiDelete = <T>(path: string, init?: RequestInit) =>
  doFetch<T>("DELETE", path, undefined, init);

/** Axios-like shape for legacy imports: `import { api } from "./client"` */
export const api = {
  get: apiGet,
  post: apiPost,
  put: apiPut,
  patch: apiPatch,
  delete: apiDelete,
};
