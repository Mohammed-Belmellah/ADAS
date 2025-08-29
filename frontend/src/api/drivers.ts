import { apiGet } from "./client";

export type CompanyDriverResponseDTO = {
  id: string;
  name: string;
  email?: string;
  // ...
};

/** Returns the number of company drivers. */
export async function countCompanyDrivers(): Promise<number> {
  const list = await apiGet<CompanyDriverResponseDTO[]>("/api/company-drivers");
  return Array.isArray(list) ? list.length : 0;
}
