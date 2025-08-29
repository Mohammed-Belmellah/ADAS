package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.DriverPerformanceDTO;
import com.example.ADAS_App.service.DriverPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverAnalyticsController {

    private final DriverPerformanceService perfService;

    // GET /api/drivers/{driverId}/performance
    // Optional time range: ?from=2025-01-01T00:00:00&to=2025-12-31T23:59:59
    @GetMapping("/{driverId}/performance")
    public DriverPerformanceDTO driverPerformance(
            @PathVariable UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return perfService.computeOverall(driverId, from, to);
    }
}
