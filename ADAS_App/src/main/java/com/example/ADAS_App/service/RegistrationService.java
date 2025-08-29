package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.registration.RegistrationDTOs;
import com.example.ADAS_App.entity.*;
import com.example.ADAS_App.repository.AdminRepository;
import com.example.ADAS_App.repository.CompanyDriverRepository;
import com.example.ADAS_App.repository.CompanyRepository;
import com.example.ADAS_App.repository.IndividualDriverRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final Keycloak kc;
    @Value("${keycloak.admin.realm}") private String realm;

    private final CompanyRepository companyRepo;
    private final CompanyDriverRepository companyDriverRepo;
    private final IndividualDriverRepository individualDriverRepo;
    private final AdminRepository adminRepo;

    // Spring Security bean from SecurityConfig
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // Use constants (or move to properties) to avoid case/name mismatches
    private static final String KC_ROLE_ADMIN  = "ADMIN";
    private static final String KC_ROLE_DRIVER = "DRIVER";

    public UUID registerCompanyAdmin(RegistrationDTOs.CompanyAdminSignup dto) {
        Company c = new Company();
        c.setName(dto.companyName());
        companyRepo.save(c);

        Admin a = new Admin();
        a.setName(dto.name());
        a.setEmail(dto.email());
        a.setRole(Role.ADMIN);
        a.setCompany(c);

        // Satisfy NOT NULL without storing plaintext
        a.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        adminRepo.save(a);

        String kcUserId = createOrAttachKeycloakUser(
                dto.email(), dto.password(), dto.name(),
                Map.of(
                        "userId",    List.of(a.getId().toString()),
                        "companyId", List.of(c.getId().toString())
                ),
                List.of(KC_ROLE_ADMIN) // ← only ADMIN in KC
        );
        a.setKeycloakUserId(kcUserId);
        adminRepo.save(a);

        return a.getId();
    }

    public UUID registerCompanyDriver(RegistrationDTOs.CompanyDriverSignup dto) {
        Company company = companyRepo.findById(dto.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        CompanyDriver d = new CompanyDriver();
        d.setName(dto.name());
        d.setEmail(dto.email());
        d.setRole(Role.DRIVER_COMPANY);              // ← domain role kept in DB
        d.setLicenseNumber(UUID.randomUUID().toString());
        d.setCompany(company);

        // Satisfy NOT NULL without storing plaintext
        d.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        companyDriverRepo.save(d);

        String kcUserId = createOrAttachKeycloakUser(
                dto.email(), dto.password(), dto.name(),
                Map.of(
                        "userId",    List.of(d.getId().toString()),
                        "driverId",  List.of(d.getId().toString()),
                        "companyId", List.of(company.getId().toString())
                ),
                List.of(KC_ROLE_DRIVER) // ← only DRIVER in KC
        );
        d.setKeycloakUserId(kcUserId);
        companyDriverRepo.save(d);

        return d.getId();
    }

    public UUID registerIndividualDriver(RegistrationDTOs.IndividualDriverSignup dto) {
        IndividualDriver d = new IndividualDriver();
        d.setName(dto.name());
        d.setEmail(dto.email());
        d.setRole(Role.DRIVER_INDEPENDENT);         // ← domain role kept in DB
        d.setLicenseNumber(UUID.randomUUID().toString());

        // Satisfy NOT NULL without storing plaintext
        d.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        individualDriverRepo.save(d);

        String kcUserId = createOrAttachKeycloakUser(
                dto.email(), dto.password(), dto.name(),
                Map.of(
                        "userId",   List.of(d.getId().toString()),
                        "driverId", List.of(d.getId().toString())
                ),
                List.of(KC_ROLE_DRIVER) // ← only DRIVER in KC
        );
        d.setKeycloakUserId(kcUserId);
        individualDriverRepo.save(d);

        return d.getId();
    }

    private String createOrAttachKeycloakUser(
            String email,
            String password,
            String first,
            Map<String, List<String>> attrs,
            List<String> realmRoles) {

        var user = new org.keycloak.representations.idm.UserRepresentation();
        user.setEnabled(true);
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(first);
        user.setEmailVerified(false);
        user.setAttributes(attrs);

        var users = kc.realm(realm).users();
        var resp = users.create(user);
        if (resp.getStatus() >= 300) {
            throw new RuntimeException("KC user create failed: " + resp.getStatus());
        }
        String userId = resp.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // Set password in KC
        var cred = new org.keycloak.representations.idm.CredentialRepresentation();
        cred.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
        cred.setTemporary(false);
        cred.setValue(password);
        users.get(userId).resetPassword(cred);

        // Assign only existing realm roles (ADMIN / DRIVER)
        if (realmRoles != null && !realmRoles.isEmpty()) {
            var reps = realmRoles.stream()
                    .map(r -> kc.realm(realm).roles().get(r).toRepresentation())
                    .toList();
            users.get(userId).roles().realmLevel().add(reps);
        }

        return userId;
    }
}
