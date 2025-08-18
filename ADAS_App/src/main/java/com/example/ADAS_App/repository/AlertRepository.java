package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByDriverId(UUID driverId);
    List<Alert> findBySessionId(UUID sessionId);
    boolean existsBySession_IdAndTypeAndCreatedAtAfter(UUID sessionId, String type, LocalDateTime after);

    List<Alert> findBySession_IdOrderByCreatedAtDesc(UUID sessionId);

    List<Alert> findBySession_Id(UUID sessionId);
}
