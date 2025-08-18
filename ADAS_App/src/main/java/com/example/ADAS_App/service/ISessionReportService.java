package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.SessionReportDTO;

import java.util.UUID;

public interface ISessionReportService {
    SessionReportDTO generate(UUID sessionId);
    SessionReportDTO getBySessionId(UUID sessionId);
}
