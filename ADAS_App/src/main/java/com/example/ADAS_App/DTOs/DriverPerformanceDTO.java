package com.example.ADAS_App.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverPerformanceDTO {
    private UUID driverId;

    // range used for the calculation (nullable means “all time”)
    private LocalDateTime from;
    private LocalDateTime to;

    // coverage
    private int sessionsCount;
    private long totalDurationSec;
    private long avgSessionDurationSec;
    private LocalDateTime latestSessionEnd;

    // weighted (by session duration) emotion share
    private Map<String, Double> emotionShareAvg;

    // KPIs (weighted by duration unless specified)
    private double safetyAvg;        // 0..100
    private double fatigueIndexAvg;  // 0..100
    private double stabilityIndexAvg;// 0..100

    // Peaks and fatigue extremes (max of streaks)
    private long peaksTotal;
    private long maxFatigueStreakSec;

    // Alerts
    private int alertsTotal;
    private Map<String, Integer> alertsByType;

    // simple trend signals (optional quick hints)
    private Double safetyLast30d;       // nullable if not enough data
    private Double safetyPrev30d;       // nullable if not enough data
}
