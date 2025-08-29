package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.CreateIndividualDriverDTO;
import com.example.ADAS_App.DTOs.IndividualDriverResponseDTO;
import com.example.ADAS_App.service.IIndividualDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/individual-drivers")
@RequiredArgsConstructor
public class IndividualDriverController {

    private final IIndividualDriverService individualDriverService;


    @PostMapping
    public ResponseEntity<IndividualDriverResponseDTO> createIndividualDriver(
            @RequestBody CreateIndividualDriverDTO dto) {
        // Keep disabled or move to /public (see variant below)
        return ResponseEntity.status(405).build();
    }


    @GetMapping
    public ResponseEntity<List<IndividualDriverResponseDTO>> getAllIndividualDrivers() {
        return ResponseEntity.status(405).build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<IndividualDriverResponseDTO> getIndividualDriverById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(individualDriverService.getIndividualDriverById(id));
    }

    /** ✅ Driver can update only their own record. Admin is NOT allowed. */

    @PutMapping("/{id}")
    public ResponseEntity<IndividualDriverResponseDTO> updateIndividualDriver(
            @PathVariable UUID id,
            @RequestBody CreateIndividualDriverDTO dto) {
        return ResponseEntity.ok(individualDriverService.updateIndividualDriver(id, dto));
    }

    /** ✅ Driver can delete only their own record. Admin is NOT allowed. */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndividualDriver(
            @PathVariable UUID id) {
        individualDriverService.deleteIndividualDriver(id);
        return ResponseEntity.noContent().build();
    }
}
