package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByDriverId(UUID driverId);

    List<Session> findTop10ByDriver_IdOrderByStartTimeDesc(UUID driverId);

    @Query("select s from Session s where s.driver.id = :driverId order by s.startTime desc")
    List<Session> findRecentByDriver(UUID driverId, org.springframework.data.domain.Pageable pageable);
    List<Session> findByEndTimeIsNullOrderByStartTimeDesc();

    // Optional: filter by driver
    List<Session> findByDriver_IdAndEndTimeIsNullOrderByStartTimeDesc(UUID driverId);

    // Optional: pageable
    Page<Session> findByEndTimeIsNull(Pageable pageable);

    List<Session> findByDriver_IdAndStartTimeBetween(UUID driverId,
                                                     LocalDateTime start,
                                                     LocalDateTime end);

    Page<Session> findByDriver_IdAndStartTimeBetween(UUID driverId,
                                                     LocalDateTime start,
                                                     LocalDateTime end,
                                                     Pageable pageable);

    // Robust “overlap” query: returns sessions that overlap a window, even if
    // they started earlier or ended later (null endTime = ongoing).
    @Query("""
      SELECT s FROM Session s
      WHERE s.driver.id = :driverId
        AND s.startTime < :windowEnd
        AND (s.endTime IS NULL OR s.endTime > :windowStart)
      ORDER BY s.startTime DESC
    """)
    List<Session> findOverlapping(UUID driverId,
                                  LocalDateTime windowStart,
                                  LocalDateTime windowEnd);

    @Query("""
      SELECT s FROM Session s
      WHERE s.driver.id = :driverId
        AND s.startTime < :windowEnd
        AND (s.endTime IS NULL OR s.endTime > :windowStart)
      ORDER BY s.startTime DESC
    """)
    Page<Session> findOverlapping(UUID driverId,
                                  LocalDateTime windowStart,
                                  LocalDateTime windowEnd,
                                  Pageable pageable);
    @Query("""
    SELECT s FROM Session s
    JOIN FETCH s.driver d
    WHERE s.endTime IS NULL
    ORDER BY s.startTime DESC
  """)
    List<Session> findActiveWithDriverFetch(); // for non-page small lists

    // Optional filter by company
    @Query("""
       SELECT s FROM Session s
       JOIN TREAT(s.driver AS CompanyDriver) cd
       WHERE s.endTime IS NULL
         AND cd.company.id = :companyId
       ORDER BY s.startTime DESC
    """)
    Page<Session> findActiveByCompany(@Param("companyId") UUID companyId, Pageable pageable);

}



