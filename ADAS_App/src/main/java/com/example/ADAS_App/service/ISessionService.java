package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.SessionDTO;

import java.util.List;
import java.util.UUID;

public interface ISessionService {
    SessionDTO createSession(SessionDTO dto);
    List<SessionDTO> getSessionsByDriver(UUID driverId);
}