// src/api/registration.ts
import { apiPost } from "./client";

export type CompanyAdminSignup = {
  name: string;
  email: string;
  password: string;
  companyName: string;
};

export type CompanyDriverSignup = {
  name: string;
  email: string;
  password: string;
  companyId: string;
};

export type IndividualDriverSignup = {
  name: string;
  email: string;
  password: string;
};

type RegisterResponse = { userId: string };

export const registerCompanyAdmin = (body: CompanyAdminSignup) =>
  apiPost<RegisterResponse>("/auth/register/company-admin", body);

export const registerCompanyDriver = (body: CompanyDriverSignup) =>
  apiPost<RegisterResponse>("/auth/register/company-driver", body);

export const registerIndividualDriver = (body: IndividualDriverSignup) =>
  apiPost<RegisterResponse>("/auth/register/individual-driver", body);
