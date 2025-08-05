package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.CreateIndividualDriverDTO;
import com.example.ADAS_App.DTOs.IndividualDriverResponseDTO;
import com.example.ADAS_App.service.IIndividualDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/individual-drivers")
@RequiredArgsConstructor
public class IndividualDriverController {

    private final IIndividualDriverService individualDriverService;

    // ✅ Create a new individual driver
    @PostMapping
    public ResponseEntity<IndividualDriverResponseDTO> createIndividualDriver(
            @RequestBody CreateIndividualDriverDTO dto) {
        return ResponseEntity.ok(individualDriverService.createIndividualDriver(dto));
    }

    // ✅ Get all individual drivers
    @GetMapping
    public ResponseEntity<List<IndividualDriverResponseDTO>> getAllIndividualDrivers() {
        return ResponseEntity.ok(individualDriverService.getAllIndividualDrivers());
    }

    // ✅ Get a specific driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<IndividualDriverResponseDTO> getIndividualDriverById(@PathVariable UUID id) {
        return ResponseEntity.ok(individualDriverService.getIndividualDriverById(id));
    }

    // ✅ Update a driver
    @PutMapping("/{id}")
    public ResponseEntity<IndividualDriverResponseDTO> updateIndividualDriver(
            @PathVariable UUID id,
            @RequestBody CreateIndividualDriverDTO dto) {
        return ResponseEntity.ok(individualDriverService.updateIndividualDriver(id, dto));
    }

    // ✅ Delete a driver
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndividualDriver(@PathVariable UUID id) {
        individualDriverService.deleteIndividualDriver(id);
        return ResponseEntity.noContent().build();
    }
}