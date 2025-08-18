package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.AlertDTO;
import com.example.ADAS_App.Mappers.AlertMapper;
import com.example.ADAS_App.entity.Alert;
import com.example.ADAS_App.entity.CompanyDriver;
import com.example.ADAS_App.entity.Session;
import com.example.ADAS_App.entity.User;
import com.example.ADAS_App.repository.AlertRepository;
import com.example.ADAS_App.repository.SessionRepository;
import com.example.ADAS_App.repository.UserRepository;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertServiceImpl implements IAlertService {

    private final AlertRepository alertRepo;
    private final UserRepository userRepo;
    private final SessionRepository sessionRepo;
    private final SimpMessagingTemplate messaging;

    public AlertServiceImpl(AlertRepository alertRepo, UserRepository userRepo , SessionRepository sessionRepo, SimpMessagingTemplate messaging) {
        this.alertRepo = alertRepo;
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.messaging = messaging;
    }

    public AlertDTO createAlert(AlertDTO dto) {
        User driver = userRepo.findById(dto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        Session session = sessionRepo.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Alert alert = new Alert();
        alert.setDriver(driver);
        alert.setSession(session);
        alert.setType(dto.getType());
        alert.setMessage(dto.getMessage());
        alert.setResolved(dto.isResolved());
        alert.setCreatedAt(dto.getCreatedAt());

        Alert save = alertRepo.save(alert);

        return AlertMapper.toDTO(save);
    }

    public List<AlertDTO> getAlertsByDriver(UUID driverId) {
        return alertRepo.findByDriverId(driverId)
                .stream()
                .map(AlertMapper::toDTO)
                .collect(Collectors.toList());
    }
    public List<Alert> getAlertsBySession(UUID sessionId) {
        return alertRepo.findBySessionId(sessionId);
    }

    public Alert createAndBroadcast(Session session, String type, String message) {
        Alert alert = alertRepo.save(
                Alert.builder()
                        .driver(session.getDriver())
                        .session(session)
                        .type(type)
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .resolved(false)
                        .build()
        );

        AlertDTO dto = AlertMapper.toDTO(alert);

        // Broadcast to useful audiences
        messaging.convertAndSend("/topic/alerts.session." + session.getId(), dto);
        messaging.convertAndSend("/topic/alerts.driver." + session.getDriver().getId(), dto);

        // If your session has a vehicle/company link:
        if (session.getDriver() instanceof CompanyDriver cd && cd.getCompany() != null) {
            messaging.convertAndSend("/topic/alerts.company." + cd.getCompany().getId(), dto);
        }

        return alert;
    }
    @Transactional(readOnly = true)
    public List<AlertDTO> listBySession(UUID sessionId) {
        return alertRepo.findBySession_IdOrderByCreatedAtDesc(sessionId)
                .stream().map(AlertMapper::toDTO).toList();
    }
    public AlertDTO markResolved(UUID alertId) {
        Alert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new NoSuchElementException("Alert not found: " + alertId));

        alert.setResolved(true);
        Alert saved = alertRepo.save(alert);

        AlertDTO dto = AlertMapper.toDTO(saved);

        // Broadcast update so dashboards refresh


        return dto;
    }
}
