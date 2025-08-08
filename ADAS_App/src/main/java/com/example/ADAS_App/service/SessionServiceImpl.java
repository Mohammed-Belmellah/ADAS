package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.SessionDTO;
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
    private final EmotionRecordRepository emotionRecordRepo;

    public SessionServiceImpl(SessionRepository sessionRepo, UserRepository userRepo, EmotionRecordRepository emotionRecordRepo) {
        this.sessionRepo = sessionRepo;
        this.userRepo = userRepo;
        this.emotionRecordRepo = emotionRecordRepo;
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
    public SessionDTO endSession(UUID id, LocalDateTime endTime) {
        Session session = sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (endTime == null) endTime = LocalDateTime.now();

        session.setEndTime(endTime);

        // Calcul automatique de averageEmotion si tu veux
        String average = emotionRecordRepo.findDominantEmotionForSession(session.getId()); // à définir
        session.setAverageEmotion(average);

        sessionRepo.save(session);

        return SessionMapper.toDTO(session);
    }
}
