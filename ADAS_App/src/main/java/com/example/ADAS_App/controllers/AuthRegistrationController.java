package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.registration.RegistrationDTOs;
import com.example.ADAS_App.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/register")
@RequiredArgsConstructor
public class AuthRegistrationController {
    private final RegistrationService svc;

    @PostMapping("/company-admin")
    public ResponseEntity<Map<String,Object>> companyAdmin(@RequestBody @Valid RegistrationDTOs.CompanyAdminSignup dto) {
        UUID userId = svc.registerCompanyAdmin(dto);
        return ResponseEntity.status(201).body(Map.of(
                "userId", userId
                // you can also add "companyId" if you want to send it back
        ));
    }

    @PostMapping("/company-driver")
    public ResponseEntity<Map<String,Object>> companyDriver(@RequestBody @Valid RegistrationDTOs.CompanyDriverSignup dto) {
        UUID userId = svc.registerCompanyDriver(dto);
        return ResponseEntity.status(201).body(Map.of(
                "userId", userId,
                "companyId", dto.companyId()
        ));
    }

    @PostMapping("/individual-driver")
    public ResponseEntity<Map<String,Object>> individual(@RequestBody @Valid RegistrationDTOs.IndividualDriverSignup dto) {
        UUID userId = svc.registerIndividualDriver(dto);
        return ResponseEntity.status(201).body(Map.of("userId", userId));
    }
}

