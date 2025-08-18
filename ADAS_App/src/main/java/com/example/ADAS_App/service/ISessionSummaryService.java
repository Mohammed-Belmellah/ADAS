package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.SessionSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface ISessionSummaryService {
    List<SessionSummaryDTO> recentByDriver(UUID driverId, int limit);
}
