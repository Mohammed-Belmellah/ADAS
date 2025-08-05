package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByDriverId(UUID driverId);
}
