package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.SessionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionReportRepository extends JpaRepository<SessionReport, UUID> {
    Optional<SessionReport> findBySession_Id(UUID sessionId);

    List<SessionReport> findAllBySession_IdIn(List<UUID> sessionIds);
    List<SessionReport> findBySession_Driver_Id(UUID driverId);
    List<SessionReport> findBySession_Driver_IdAndSession_EndTimeBetween(
            UUID driverId, LocalDateTime from, LocalDateTime to);
}

