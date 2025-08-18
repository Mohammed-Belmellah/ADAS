package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.SessionSummaryDTO;
import com.example.ADAS_App.entity.Session;
import com.example.ADAS_App.entity.SessionReport;
import com.example.ADAS_App.repository.SessionReportRepository;
import com.example.ADAS_App.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionSummaryServiceImpl implements ISessionSummaryService {

    private final SessionRepository sessionRepo;
    private final SessionReportRepository reportRepo;

    @Override
    public List<SessionSummaryDTO> recentByDriver(UUID driverId, int limit) {
        var sessions = sessionRepo.findRecentByDriver(driverId, PageRequest.of(0, Math.max(1, limit)));
        if (sessions.isEmpty()) return List.of();

        // Load reports in batch to avoid N+1
        Map<UUID, SessionReport> reportBySessionId = reportRepo
                .findAllBySession_IdIn(sessions.stream().map(Session::getId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(r -> r.getSession().getId(), r -> r));

        List<SessionSummaryDTO> list = new ArrayList<>(sessions.size());
        for (Session s : sessions) {
            SessionReport r = reportBySessionId.get(s.getId());
            Long duration = (s.getStartTime() != null && s.getEndTime() != null)
                    ? Math.max(0, Duration.between(s.getStartTime(), s.getEndTime()).getSeconds())
                    : null;

            list.add(SessionSummaryDTO.builder()
                    .sessionId(s.getId())
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .dominantEmotion(r != null ? r.getDominantEmotion() : null)
                    .safetyEmotionScore(r != null ? r.getSafetyEmotionScore() : null)
                    .emotionShare(r != null ? r.getEmotionShare() : Map.of())  // empty if report missing
                    .durationSec(duration)
                    .build());
        }
        return list;
    }
}
