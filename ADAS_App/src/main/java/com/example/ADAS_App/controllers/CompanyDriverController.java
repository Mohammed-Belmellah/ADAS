package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.CompanyDriverResponseDTO;
import com.example.ADAS_App.DTOs.CreateCompanyDriverDTO;
import com.example.ADAS_App.service.ICompanyDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/company-drivers")
@RequiredArgsConstructor
public class CompanyDriverController {

    private final ICompanyDriverService companyDriverService;

    // ✅ Create a new company driver
    @PostMapping
    public ResponseEntity<CompanyDriverResponseDTO> createCompanyDriver(
            @RequestBody CreateCompanyDriverDTO dto) {
        return ResponseEntity.ok(companyDriverService.createCompanyDriver(dto));
    }

    // ✅ Get all company drivers
    @GetMapping
    public ResponseEntity<List<CompanyDriverResponseDTO>> getAllCompanyDrivers() {
        return ResponseEntity.ok(companyDriverService.getAllCompanyDrivers());
    }

    // ✅ Get a specific company driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDriverResponseDTO> getCompanyDriverById(@PathVariable UUID id) {
        return ResponseEntity.ok(companyDriverService.getCompanyDriverById(id));
    }

    // ✅ Update a company driver
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDriverResponseDTO> updateCompanyDriver(
            @PathVariable UUID id,
            @RequestBody CreateCompanyDriverDTO dto) {
        return ResponseEntity.ok(companyDriverService.updateCompanyDriver(id, dto));
    }

    // ✅ Delete a company driver
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompanyDriver(@PathVariable UUID id) {
        companyDriverService.deleteCompanyDriver(id);
        return ResponseEntity.noContent().build();
    }
}
