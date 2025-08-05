package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.EmotionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface EmotionRecordRepository extends JpaRepository<EmotionRecord, UUID> {


    List<EmotionRecord> findBySessionId(UUID sessionId);
}
