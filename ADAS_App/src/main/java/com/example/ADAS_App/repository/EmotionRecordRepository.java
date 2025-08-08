package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.EmotionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface EmotionRecordRepository extends JpaRepository<EmotionRecord, UUID> {


    List<EmotionRecord> findBySessionId(UUID sessionId);
    @Query(value = """
        SELECT el.emotion
        FROM emotion_labels el
        JOIN emotion_records er ON el.emotion_record_id = er.id
        WHERE er.session_id = :sessionId
        GROUP BY el.emotion
        ORDER BY COUNT(*) DESC
        LIMIT 1
        """, nativeQuery = true)
    String findDominantEmotionForSession(@Param("sessionId") UUID sessionId);
}
