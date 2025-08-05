package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByDriverId(UUID driverId);
}
