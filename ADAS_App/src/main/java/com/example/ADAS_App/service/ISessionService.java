package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.ActiveSessionDetailsDTO;
import com.example.ADAS_App.DTOs.EndSessionResponse;
import com.example.ADAS_App.DTOs.SessionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public interface ISessionService {
    SessionDTO createSession(SessionDTO dto);
    List<SessionDTO> getSessionsByDriver(UUID driverId);
    EndSessionResponse endSessionWithReport(UUID id, LocalDateTime endTime);

    List<SessionDTO> getActiveSessions();

    List<SessionDTO> getActiveSessionsByDriver(UUID driverId);

    List<SessionDTO> getDriverSessionsByDate(UUID driverId, LocalDate date, ZoneId zone);
    Page<SessionDTO> getDriverSessionsByRange(UUID driverId, LocalDate from, LocalDate to, ZoneId zone, Pageable pageable);
    Page<SessionDTO> getDriverSessionsByStartBetween(
            UUID driverId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
    Page<ActiveSessionDetailsDTO> getActiveSessionsWithDetails(UUID companyId, Pageable pageable);

}