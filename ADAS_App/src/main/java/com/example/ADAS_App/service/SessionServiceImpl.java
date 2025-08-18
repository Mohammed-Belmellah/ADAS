package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.EndSessionResponse;
import com.example.ADAS_App.DTOs.SessionDTO;
import com.example.ADAS_App.DTOs.SessionReportDTO;
import com.example.ADAS_App.Mappers.SessionMapper;
import com.example.ADAS_App.entity.Session;
import com.example.ADAS_App.entity.User;
import com.example.ADAS_App.repository.EmotionRecordRepository;
import com.example.ADAS_App.repository.SessionRepository;
import com.example.ADAS_App.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionServiceImpl implements ISessionService {

    private final SessionRepository sessionRepo;
    private final UserRepository userRepo;
    private final SessionReportServiceImpl sessionReportService;

    public SessionServiceImpl(SessionRepository sessionRepo, UserRepository userRepo, SessionReportServiceImpl sessionReportService ) {
        this.sessionRepo = sessionRepo;
        this.userRepo = userRepo;
        this.sessionReportService = sessionReportService;
    }

    public SessionDTO createSession(SessionDTO dto) {
        User driver = userRepo.findById(dto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Session session = new Session();
        session.setDriver(driver);
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setAverageEmotion(dto.getAverageEmotion());

        return SessionMapper.toDTO(sessionRepo.save(session));
    }

    public List<SessionDTO> getSessionsByDriver(UUID driverId) {
        return sessionRepo.findByDriverId(driverId)
                .stream()
                .map(SessionMapper::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    public EndSessionResponse endSessionWithReport(UUID id, LocalDateTime endTime) {
        Session session = sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (endTime == null) endTime = LocalDateTime.now();
        session.setEndTime(endTime);
        sessionRepo.save(session);

        // Compute report
        SessionReportDTO report = sessionReportService.generate(id);

        // Update averageEmotion from the report
        session.setAverageEmotion(report.getDominantEmotion());
        sessionRepo.save(session);

        return new EndSessionResponse(SessionMapper.toDTO(session), report);
    }
}
