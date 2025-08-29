package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.ActiveSessionDetailsDTO;
import com.example.ADAS_App.DTOs.EndSessionResponse;
import com.example.ADAS_App.DTOs.SessionDTO;
import com.example.ADAS_App.Mappers.SessionMapper;
import com.example.ADAS_App.repository.SessionRepository;
import com.example.ADAS_App.service.ISessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ISessionService sessionService;
    private final SessionRepository sessionRepo;

    /** Create a session.
     *  DRIVER: only for self (dto.driverId).
     *  ADMIN: only for company drivers.
     */

    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@RequestBody SessionDTO dto) {
        return ResponseEntity.ok(sessionService.createSession(dto));
    }

    /** All sessions of a driver */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt)) 
      or (hasRole('ADMIN') and @accessGuard.adminCanAccess(#driverId))
    """)
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByDriver(@PathVariable UUID driverId,
                                                                @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(sessionService.getSessionsByDriver(driverId));
    }

    /** End a session + persist report.
     *  Commonly ADMIN-only. If you prefer the same rule as reads, replace with @accessGuard.canAccessSession(#id, #jwt).
     */

    @PutMapping("/{id}/end-with-report")
    public EndSessionResponse endSessionWithReport(@PathVariable UUID id,
                                                   @RequestBody(required = false) Map<String, Object> body) {
        LocalDateTime endTime = null;
        if (body != null && body.containsKey("endTime")) {
            endTime = LocalDateTime.parse(body.get("endTime").toString());
        }
        return sessionService.endSessionWithReport(id, endTime);
    }

    /** Active sessions list — ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public List<SessionDTO> getActiveSessions() {
        return sessionService.getActiveSessions();
    }

    /** Active sessions for a specific driver — DRIVER only self, ADMIN allowed */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt)) 
      or hasRole('ADMIN')
    """)
    @GetMapping(value = "/active", params = "driverId")
    public List<SessionDTO> getActiveSessionsByDriver(@RequestParam UUID driverId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return sessionService.getActiveSessionsByDriver(driverId);
    }

    /** Paged active sessions — ADMIN only */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active/page")
    public Page<SessionDTO> getActiveSessionsPaged(
            @PageableDefault(sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return sessionRepo.findByEndTimeIsNull(pageable).map(SessionMapper::toDTO);
    }

    /** Sessions of a driver by a specific date */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt)) 
      or (hasRole('ADMIN') and @accessGuard.adminCanAccess(#driverId))
    """)
    @GetMapping("/{driverId}/sessions/by-date")
    public List<SessionDTO> byDate(
            @PathVariable UUID driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "zone", required = false) String zoneId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ZoneId zone = zoneId != null ? ZoneId.of(zoneId) : ZoneId.systemDefault();
        return sessionService.getDriverSessionsByDate(driverId, date, zone);
    }

    /** Sessions of a driver in a date range */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt)) 
      or (hasRole('ADMIN') and @accessGuard.adminCanAccess(#driverId))
    """)
    @GetMapping("/{driverId}/sessions/range")
    public Page<SessionDTO> byRange(
            @PathVariable UUID driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "zone", required = false) String zoneId,
            @PageableDefault(sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ZoneId zone = zoneId != null ? ZoneId.of(zoneId) : ZoneId.systemDefault();
        return sessionService.getDriverSessionsByRange(driverId, from, to, zone, pageable);
    }

    /** Sessions of a driver between start/end instants */
    @PreAuthorize("""
      (hasRole('DRIVER') and @accessGuard.isSelf(#driverId, #jwt)) 
      or (hasRole('ADMIN') and @accessGuard.adminCanAccess(#driverId))
    """)
    @GetMapping("/{driverId}/sessions")
    public Page<SessionDTO> sessionsByStartBetween(
            @PathVariable UUID driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return sessionService.getDriverSessionsByStartBetween(driverId, start, end, pageable);
    }

    /** Active sessions with details — ADMIN only (dashboard).
     *  If you later want drivers to see company-scoped results, we can match by token companyId.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active/details")
    public Page<ActiveSessionDetailsDTO> activeWithDetails(
            @RequestParam(required = false) UUID companyId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return sessionService.getActiveSessionsWithDetails(companyId, pageable);
    }
}
