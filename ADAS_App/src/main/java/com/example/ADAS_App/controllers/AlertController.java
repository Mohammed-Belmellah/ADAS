package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.AlertDTO;
import com.example.ADAS_App.service.IAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final IAlertService alertService;

    public AlertController(IAlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<AlertDTO> createAlert(@RequestBody AlertDTO dto) {
        return ResponseEntity.ok(alertService.createAlert(dto));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<AlertDTO>> getAlertsByDriver(@PathVariable UUID driverId) {
        return ResponseEntity.ok(alertService.getAlertsByDriver(driverId));
    }
}
