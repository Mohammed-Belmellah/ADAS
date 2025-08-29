package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.SessionReportDTO;
import com.example.ADAS_App.service.ISessionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionReportController {

    private final ISessionReportService sessionReportService;

    /** Generate (compute & persist) a session report.
     *  Policy: DRIVER only for their own session; ADMIN only if the session’s driver is a company driver.
     *  If you want ADMIN-only generation, see the variant below.
     */

    @PostMapping("/{sessionId}/report")
    public SessionReportDTO generate(@PathVariable UUID sessionId) {
        return sessionReportService.generate(sessionId);
    }

    /** Fetch an existing report.
     *  Same access policy as generate().
     */
    @PreAuthorize("@accessGuard.canAccessSession(#sessionId, #jwt)")
    @GetMapping("/{sessionId}/report")
    public SessionReportDTO get(@PathVariable UUID sessionId,
                                @AuthenticationPrincipal Jwt jwt) {
        return sessionReportService.getBySessionId(sessionId);
    }
}
