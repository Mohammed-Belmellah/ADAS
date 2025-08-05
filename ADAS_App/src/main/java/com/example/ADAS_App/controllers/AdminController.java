package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.AdminResponseDTO;
import com.example.ADAS_App.DTOs.CreateAdminDTO;
import com.example.ADAS_App.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;

    @PostMapping
    public ResponseEntity<AdminResponseDTO> createAdmin(@RequestBody CreateAdminDTO dto) {
        return ResponseEntity.ok(adminService.createAdmin(dto));
    }

    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getAdminById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> updateAdmin(@PathVariable UUID id,
                                                        @RequestBody CreateAdminDTO dto) {
        return ResponseEntity.ok(adminService.updateAdmin(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable UUID id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}