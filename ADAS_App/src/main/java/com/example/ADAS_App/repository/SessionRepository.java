package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByDriverId(UUID driverId);

    List<Session> findTop10ByDriver_IdOrderByStartTimeDesc(UUID driverId);

    @Query("select s from Session s where s.driver.id = :driverId order by s.startTime desc")
    List<Session> findRecentByDriver(UUID driverId, org.springframework.data.domain.Pageable pageable);
}

