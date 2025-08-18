package com.example.ADAS_App.DTOs;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class SessionReportDTO {
    private UUID sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long durationSec;

    private Map<String, Double> emotionShare;  // 0.0..1.0 (ou % si tu préfères)
    private Map<String, Double> confAvg;
    private String dominantEmotion;

    private Long fatigueCumSec;
    private Long fatigueMaxStreakSec;
    private Long stressCumSec;
    private Long angerCumSec;
    private Integer peaksCount;

    private Integer safetyEmotionScore;  // 0..100
    private Integer fatigueIndex;        // 0..100
    private Integer stabilityIndex;

    private Map<String, Integer> alertsCount;
    private Integer totalAlerts;              // new
    private Integer unresolvedAlerts;

    // optionnels UI
    // private Map<String, Integer> emotionCount;
    // private List<String> recommendations;
}
