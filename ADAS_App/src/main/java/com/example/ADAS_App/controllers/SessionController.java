package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.EndSessionResponse;
import com.example.ADAS_App.DTOs.SessionDTO;
import com.example.ADAS_App.service.ISessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final ISessionService sessionService;

    public SessionController(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@RequestBody SessionDTO dto) {
        return ResponseEntity.ok(sessionService.createSession(dto));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByDriver(@PathVariable UUID driverId) {
        return ResponseEntity.ok(sessionService.getSessionsByDriver(driverId));
    }
    @PutMapping("/{id}/end-with-report")
    public EndSessionResponse endSessionWithReport(@PathVariable UUID id,
                                                   @RequestBody(required = false) Map<String, Object> body) {
        LocalDateTime endTime = null;
        if (body != null && body.containsKey("endTime")) {
            endTime = LocalDateTime.parse(body.get("endTime").toString());
        }
        return sessionService.endSessionWithReport(id, endTime);
    }

}