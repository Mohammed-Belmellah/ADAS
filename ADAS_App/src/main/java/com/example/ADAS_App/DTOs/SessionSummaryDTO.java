package com.example.ADAS_App.DTOs;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SessionSummaryDTO {
    private UUID sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String dominantEmotion;          // from SessionReport
    private Integer safetyEmotionScore;      // 0..100
    private Map<String, Double> emotionShare; // small map (0..1 fractions)

    private Long durationSec;                // convenience
}