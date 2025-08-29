package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.AlertDTO;
import com.example.ADAS_App.service.IAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final IAlertService alertService;

    @PostMapping
    public ResponseEntity<AlertDTO> createAlert(@RequestBody AlertDTO dto) {
        return ResponseEntity.ok(alertService.createAlert(dto));
    }

    /** Alerts by driver: DRIVER only self, ADMIN only company drivers */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt))
      or (hasRole('ADMIN') and @accessGuard.adminCanAccess(#driverId))
    """)
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<AlertDTO>> getAlertsByDriver(@PathVariable UUID driverId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(alertService.getAlertsByDriver(driverId));
    }

    /** Alerts by session: same policy as session access */
    @PreAuthorize("@accessGuard.canAccessSession(#sessionId, #jwt)")
    @GetMapping("/session/{sessionId}")
    public List<AlertDTO> listBySession(@PathVariable UUID sessionId,
                                        @AuthenticationPrincipal Jwt jwt) {
        return alertService.listBySession(sessionId);
    }

    /** Resolve alert.
     *  Choose policy:
     *  A) ADMIN-only (recommended)   -> keep @PreAuthorize("hasRole('ADMIN')")
     *  B) Session policy (driver can resolve own) -> use @accessGuard.canAccessSession(...)
     */

    @PreAuthorize("@accessGuard.canAccessSession(@alertService.getSessionIdOfAlert(#id), #jwt)")
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<AlertDTO> resolve(@PathVariable UUID id,
                                            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(alertService.markResolved(id));
    }
}
