package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.EndSessionResponse;
import com.example.ADAS_App.DTOs.SessionDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ISessionService {
    SessionDTO createSession(SessionDTO dto);
    List<SessionDTO> getSessionsByDriver(UUID driverId);
    EndSessionResponse endSessionWithReport(UUID id, LocalDateTime endTime);
}