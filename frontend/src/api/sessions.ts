// src/api/sessions.ts
import { apiGet, apiPost, apiPut } from "./client";

/** ---- Types (from your Swagger) ---- */
export type SessionDTO = {
  id: string;
  driverId: string;
  startTime: string;               // ISO
  endTime?: string | null;         // ISO | null when active
  averageEmotion?: string | null;
};

export type SessionSummaryDTO = {
  sessionId: string;
  startTime: string;
  endTime?: string | null;
  dominantEmotion?: string | null;
  safetyEmotionScore?: number | null;
  emotionShare?: Record<string, number>;
  durationSec?: number;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // 0-based page index
  size: number;         // page size
  first: boolean;
  last: boolean;
  numberOfElements: number;
};

export type SessionReportDTO = {
  sessionId: string;
  startTime: string;
  endTime: string;
  durationSec: number;
  emotionShare: Record<string, number>;
  confAvg: Record<string, number>;
  dominantEmotion: string;
  fatigueCumSec: number;
  fatigueMaxStreakSec: number;
  stressCumSec: number;
  angerCumSec: number;
  peaksCount: number;
  safetyEmotionScore: number;
  fatigueIndex: number;
  stabilityIndex: number;
  alertsCount: Record<string, number>;
  totalAlerts: number;
  unresolvedAlerts: number;
};
export type AlertDTO = {
  id: string;
  driverId: string;
  sessionId: string;
  type: string;
  message?: string;
  resolved: boolean;
  createdAt: string; // ISO
};
export type DriverBriefDTO = {
  id: string;
  fullName?: string;
  phone?: string;
};

export type ActiveSessionDetailsDTO = {
  sessionId: string;
  startTime: string;              // ISO
  averageEmotion?: string | null; // optional
  driver: DriverBriefDTO;
  unresolvedAlertsCount?: number | null;
  alerts?: AlertDTO[];            // may be limited (recent X)
};

/** ---- Utils ---- */
function qp(params: Record<string, any>) {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === "") return;
    if (Array.isArray(v)) v.forEach((x) => p.append(k, String(x)));
    else p.append(k, String(v));
  });
  return p.toString();
}
export type PageActiveSessionDetailsDTO = Page<ActiveSessionDetailsDTO>;
/** ---- Endpoints ---- */
export async function getActiveSessionsWithDetailsPaged(
  companyId?: string,
  page = 0,
  size = 10,
  sort: string[] = ["startTime,desc"]
): Promise<PageActiveSessionDetailsDTO> {
  const params: Record<string, any> = { page, size, sort };
  if (companyId) params.companyId = companyId;

  const qs = new URLSearchParams(
    Object.entries(params).flatMap(([k, v]) =>
      Array.isArray(v) ? v.map((vv) => [k, String(vv)]) : [[k, String(v)]]
    ) as any
  ).toString();

  return apiGet<PageActiveSessionDetailsDTO>(`/api/sessions/active/details?${qs}`);
}
/** Paged listing of active sessions (no driver filter). */
export async function getActiveSessionsPaged(
  page = 0,
  size = 10,
  sort?: string[]            // e.g. ["startTime,desc"]
): Promise<Page<SessionDTO>> {
  return apiGet<Page<SessionDTO>>(
    `/api/sessions/active/page?${qp({ page, size, sort })}`
  );
}

/** Quick count of all active sessions (cheap: size=1). */
export async function countActiveSessions(): Promise<number> {
  const page = await getActiveSessionsPaged(0, 1);
  return page.totalElements ?? 0;
}

/** Active sessions for a given driver. */
export async function getActiveSessionsByDriver(
  driverId: string
): Promise<SessionDTO[]> {
  return apiGet<SessionDTO[]>(
    `/api/sessions/active?${qp({ driverId })}`
  );
}

/** All sessions for a driver (non-paged). */
export async function getSessionsByDriver(
  driverId: string
): Promise<SessionDTO[]> {
  return apiGet<SessionDTO[]>(`/api/sessions/driver/${driverId}`);
}

/** Sessions for a driver on a specific date (YYYY-MM-DD). */
export async function getSessionsByDate(
  driverId: string,
  date: string,                 // format: date (YYYY-MM-DD)
  zone?: string                 // optional IANA TZ
): Promise<SessionDTO[]> {
  return apiGet<SessionDTO[]>(
    `/api/sessions/${driverId}/sessions/by-date?${qp({ date, zone })}`
  );
}

/** Paged sessions for a driver in a date range (YYYY-MM-DD). */
export async function getSessionsByRange(
  driverId: string,
  from: string,                 // format: date
  to: string,                   // format: date
  page = 0,
  size = 10,
  zone?: string,
  sort: string[] = ["startTime,desc"]
): Promise<Page<SessionDTO>> {
  return apiGet<Page<SessionDTO>>(
    `/api/sessions/${driverId}/sessions/range?${qp({
      from, to, zone, page, size, sort,
    })}`
  );
}

/** 🔥 NEW: Paged sessions for a driver between start & end datetime. */
export async function getSessionsByStartBetween(
  driverId: string,
  start: string,                 // format: date-time (ISO)
  end: string,                   // format: date-time (ISO)
  page = 0,
  size = 10,
  sort: string[] = ["startTime,desc"]
): Promise<Page<SessionDTO>> {
  return apiGet<Page<SessionDTO>>(
    `/api/sessions/${driverId}/sessions?${qp({
      start, end, page, size, sort,
    })}`
  );
}

/** Recent session summaries for a driver (limit default 10). */
export async function getRecentSessions(
  driverId: string,
  limit = 10
): Promise<SessionSummaryDTO[]> {
  return apiGet<SessionSummaryDTO[]>(
    `/api/sessions/drivers/${driverId}/recent?${qp({ limit })}`
  );
}

/** Latest session summary for a driver. */
export async function getLatestSessionSummary(
  driverId: string
): Promise<SessionSummaryDTO> {
  return apiGet<SessionSummaryDTO>(
    `/api/sessions/drivers/${driverId}/latest`
  );
}

/** Fetch an existing report for a session (GET). */
export async function getSessionReport(
  sessionId: string
): Promise<SessionReportDTO> {
  return apiGet<SessionReportDTO>(`/api/sessions/${sessionId}/report`);
}

/** Helper: check if report exists (GET 200 vs error). */
export async function hasSessionReport(sessionId: string): Promise<boolean> {
  try {
    await getSessionReport(sessionId);
    return true;
  } catch {
    return false;
  }
}

/** Generate a report for a session (POST). */
export async function generateSessionReport(
  sessionId: string
): Promise<SessionReportDTO> {
  return apiPost<SessionReportDTO>(`/api/sessions/${sessionId}/report`);
}

/** End a session and return its report (PUT). */
export async function endSessionWithReport(
  sessionId: string
): Promise<{ session: SessionDTO; report: SessionReportDTO }> {
  return apiPut<{ session: SessionDTO; report: SessionReportDTO }>(
    `/api/sessions/${sessionId}/end-with-report`
  );
}
export async function getSessionAlerts(
  sessionId: string,
  limit = 50
): Promise<AlertDTO[]> {
  // 🔧 change the path if your backend uses a different route
  return apiGet<AlertDTO[]>(`/api/alerts/session/${sessionId}?limit=${limit}`);
}
