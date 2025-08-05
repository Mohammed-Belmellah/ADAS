package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.EmergencyContactDTO;
import com.example.ADAS_App.service.IEmergencyContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
public class EmergencyContactController {

    private final IEmergencyContactService contactService;

    public EmergencyContactController(IEmergencyContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<EmergencyContactDTO> addContact(@RequestBody EmergencyContactDTO dto) {
        return ResponseEntity.ok(contactService.addContact(dto));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<EmergencyContactDTO>> getContactsByDriver(@PathVariable UUID driverId) {
        return ResponseEntity.ok(contactService.getContactsByDriverId(driverId));
    }
}
