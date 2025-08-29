package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.*;
import com.example.ADAS_App.Mappers.AlertMapper;
import com.example.ADAS_App.Mappers.CompanyDriverMapper;
import com.example.ADAS_App.Mappers.SessionMapper;
import com.example.ADAS_App.entity.Alert;
import com.example.ADAS_App.entity.Session;
import com.example.ADAS_App.entity.User;
import com.example.ADAS_App.repository.AlertRepository;
import com.example.ADAS_App.repository.SessionRepository;
import com.example.ADAS_App.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionServiceImpl implements ISessionService {

    private final SessionRepository sessionRepo;
    private final UserRepository userRepo;
    private final SessionReportServiceImpl sessionReportService;
    private final AlertRepository alertRepo;



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
    @Override
    public List<SessionDTO> getActiveSessions() {
        return sessionRepo.findByEndTimeIsNullOrderByStartTimeDesc()
                .stream()
                .map(SessionMapper::toDTO)
                .toList();
    }

    @Override
    public List<SessionDTO> getActiveSessionsByDriver(UUID driverId) {
        return sessionRepo.findByDriver_IdAndEndTimeIsNullOrderByStartTimeDesc(driverId)
                .stream()
                .map(SessionMapper::toDTO)
                .toList();
    }
    @Override
    public List<SessionDTO> getDriverSessionsByDate(UUID driverId, LocalDate date, ZoneId zone) {
        LocalDateTime start = date.atStartOfDay(zone).toLocalDateTime();
        LocalDateTime end   = date.plusDays(1).atStartOfDay(zone).toLocalDateTime();
        return sessionRepo.findOverlapping(driverId, start, end)
                .stream()
                .map(SessionMapper::toDTO)
                .toList();
    }

    @Override
    public Page<SessionDTO> getDriverSessionsByRange(UUID driverId, LocalDate from, LocalDate to, ZoneId zone, Pageable pageable) {
        // Window is [from 00:00, (to+1) 00:00) in the specified zone
        LocalDateTime winStart = from.atStartOfDay(zone).toLocalDateTime();
        LocalDateTime winEnd   = to.plusDays(1).atStartOfDay(zone).toLocalDateTime();
        return sessionRepo.findOverlapping(driverId, winStart, winEnd, pageable)
                .map(SessionMapper::toDTO);
    }

    @Override
    public Page<SessionDTO> getDriverSessionsByStartBetween(
            UUID driverId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {

        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Invalid time window: 'start' must be before 'end'.");
        }

        return sessionRepo
                .findByDriver_IdAndStartTimeBetween(driverId, start, end, pageable)
                .map(SessionMapper::toDTO);
    }
    @Override
    public Page<ActiveSessionDetailsDTO> getActiveSessionsWithDetails(UUID companyId, Pageable pageable) {
        Page<Session> page = (companyId == null)
                ? sessionRepo.findByEndTimeIsNull(pageable)
                : sessionRepo.findActiveByCompany(companyId, pageable);

        // Collect session IDs
        List<UUID> sessionIds = page.getContent().stream().map(Session::getId).toList();
        if (sessionIds.isEmpty()) {
            return page.map(s -> toDetails(s, List.of()));
        }

        // Batch load alerts for these sessions
        List<Alert> alerts = alertRepo.findBySession_IdIn(sessionIds);
        Map<UUID, List<Alert>> bySession = alerts.stream().collect(
                Collectors.groupingBy(a -> a.getSession().getId(), LinkedHashMap::new, Collectors.toList())
        );

        // Map to DTOs
        return page.map(s -> {
            List<Alert> list = bySession.getOrDefault(s.getId(), List.of());
            int unresolved = (int) list.stream().filter(a -> !a.isResolved()).count();
            List<AlertDTO> alertDTOs = list.stream()
                    .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                    .map(AlertMapper::toDTO)
                    .toList();
            return toDetails(s, alertDTOs, unresolved);
        });
    }

    private ActiveSessionDetailsDTO toDetails(Session s, List<AlertDTO> alerts) {
        return toDetails(s, alerts, (int) alerts.stream().filter(a -> !a.isResolved()).count());
    }

    private ActiveSessionDetailsDTO toDetails(Session s, List<AlertDTO> alerts, int unresolved) {
        return ActiveSessionDetailsDTO.builder()
                .sessionId(s.getId())
                .startTime(s.getStartTime())
                .averageEmotion(s.getAverageEmotion())
                .driver(CompanyDriverMapper.toBrief(s.getDriver()))
                .unresolvedAlertsCount(unresolved)
                .alerts(alerts)
                .build();
    }
}
