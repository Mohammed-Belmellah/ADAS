package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.CompanyDriverResponseDTO;
import com.example.ADAS_App.DTOs.CreateCompanyDriverDTO;
import com.example.ADAS_App.service.ICompanyDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/company-drivers")
@RequiredArgsConstructor
public class CompanyDriverController {

    private final ICompanyDriverService companyDriverService;


    @PostMapping
    public ResponseEntity<CompanyDriverResponseDTO> createCompanyDriver(
            @RequestBody CreateCompanyDriverDTO dto) {
        return ResponseEntity.ok(companyDriverService.createCompanyDriver(dto));
    }

    /** List all company drivers — ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CompanyDriverResponseDTO>> getAllCompanyDrivers() {
        return ResponseEntity.ok(companyDriverService.getAllCompanyDrivers());
    }

    /** Get a specific company driver:
     *  - ADMIN → allowed
     *  - DRIVER → only self
     */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @accessGuard.isSelf(#id, #jwt))")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDriverResponseDTO> getCompanyDriverById(@PathVariable UUID id,
                                                                         @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(companyDriverService.getCompanyDriverById(id));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDriverResponseDTO> updateCompanyDriver(
            @PathVariable UUID id,
            @RequestBody CreateCompanyDriverDTO dto) {
        return ResponseEntity.ok(companyDriverService.updateCompanyDriver(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompanyDriver(@PathVariable UUID id) {
        companyDriverService.deleteCompanyDriver(id);
        return ResponseEntity.noContent().build();
    }
}
