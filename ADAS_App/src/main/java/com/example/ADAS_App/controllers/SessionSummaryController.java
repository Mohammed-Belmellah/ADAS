package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.SessionSummaryDTO;
import com.example.ADAS_App.service.ISessionSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionSummaryController {

    private final ISessionSummaryService summaryService;

    /** GET /api/sessions/drivers/{driverId}/recent?limit=10 */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt))
      or (hasRole('ADMIN') and @accessGuard.adminCanAccess(#driverId))
    """)
    @GetMapping("/drivers/{driverId}/recent")
    public ResponseEntity<List<SessionSummaryDTO>> recentByDriver(
            @PathVariable UUID driverId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(summaryService.recentByDriver(driverId, limit));
    }

    /** GET /api/sessions/drivers/{driverId}/latest */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt))
      or (hasRole('ADMIN') and @accessGuard.adminCanAccess(#driverId))
    """)
    @GetMapping("/drivers/{driverId}/latest")
    public ResponseEntity<SessionSummaryDTO> latest(
            @PathVariable UUID driverId,
            @AuthenticationPrincipal Jwt jwt) {
        var list = summaryService.recentByDriver(driverId, 1);
        return list.isEmpty() ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(list.get(0));
    }
}
