import { apiGet } from "./client";

export type DriverPerformanceDTO = {
  driverId: string;
  from?: string | null;
  to?: string | null;

  sessionsCount: number;
  totalDurationSec: number;
  avgSessionDurationSec: number;
  latestSessionEnd?: string | null;

  emotionShareAvg: Record<string, number>;

  safetyAvg: number;           // 0..100
  fatigueIndexAvg: number;     // 0..100
  stabilityIndexAvg: number;   // 0..100

  peaksTotal: number;
  maxFatigueStreakSec: number;

  alertsTotal: number;
  alertsByType: Record<string, number>;

  safetyLast30d?: number | null;
  safetyPrev30d?: number | null;
};

export async function getDriverPerformance(
  driverId: string,
  fromISO?: string,
  toISO?: string
): Promise<DriverPerformanceDTO> {
  const qs = new URLSearchParams();
  if (fromISO) qs.set("from", fromISO);
  if (toISO) qs.set("to", toISO);
  const path = `/api/drivers/${driverId}/performance${qs.toString() ? `?${qs.toString()}` : ""}`;
  return apiGet<DriverPerformanceDTO>(path);
}
