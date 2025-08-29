// src/auth/loginWithHint.ts
import { keycloak } from "./keycloak";
export function loginWithHint(email?: string) {
  keycloak.login({ loginHint: email });
}
