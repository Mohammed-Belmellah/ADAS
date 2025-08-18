package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.SessionReportDTO;
import com.example.ADAS_App.service.ISessionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionReportController {

    private final ISessionReportService sessionReportService;

    // Generate (compute & persist). Use POST because it mutates state.
    @PostMapping("/{sessionId}/report")
    public SessionReportDTO generate(@PathVariable UUID sessionId) {
        return sessionReportService.generate(sessionId);
    }

    // Fetch existing report only (no recompute). Use GET.
    @GetMapping("/{sessionId}/report")
    public SessionReportDTO get(@PathVariable UUID sessionId) {
        return sessionReportService.getBySessionId(sessionId);
    }
}
