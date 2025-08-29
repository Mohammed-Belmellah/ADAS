package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.DriverPerformanceDTO;
import com.example.ADAS_App.entity.SessionReport;
import com.example.ADAS_App.repository.SessionReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverPerformanceService {

    private final SessionReportRepository reportRepo;

    public DriverPerformanceDTO computeOverall(UUID driverId, LocalDateTime from, LocalDateTime to) {
        final List<SessionReport> reports = (from != null && to != null)
                ? reportRepo.findBySession_Driver_IdAndSession_EndTimeBetween(driverId, from, to)
                : reportRepo.findBySession_Driver_Id(driverId);

        if (reports.isEmpty()) {
            return DriverPerformanceDTO.builder()
                    .driverId(driverId)
                    .from(from).to(to)
                    .sessionsCount(0)
                    .totalDurationSec(0)
                    .avgSessionDurationSec(0)
                    .emotionShareAvg(Collections.emptyMap())
                    .safetyAvg(0).fatigueIndexAvg(0).stabilityIndexAvg(0)
                    .peaksTotal(0).maxFatigueStreakSec(0)
                    .alertsTotal(0).alertsByType(Collections.emptyMap())
                    .build();
        }

        // Coverage
        int sessionsCount = reports.size();
        long totalDuration = reports.stream().mapToLong(r -> Optional.ofNullable(r.getDurationSec()).orElse(0L)).sum();
        long avgDuration = sessionsCount == 0 ? 0 : Math.round((double) totalDuration / sessionsCount);
        LocalDateTime latestEnd = reports.stream()
                .map(r -> r.getSession().getEndTime())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Weighted emotion share
        Map<String, Double> weightedEmotion = new HashMap<>();
        if (totalDuration > 0) {
            for (SessionReport r : reports) {
                long d = Optional.ofNullable(r.getDurationSec()).orElse(0L);
                Map<String, Double> share = Optional.ofNullable(r.getEmotionShare()).orElse(Map.of());
                final double w = (double) d / totalDuration;
                for (var e : share.entrySet()) {
                    weightedEmotion.merge(e.getKey(), w * e.getValue(), Double::sum);
                }
            }
        }

        // Weighted averages of scores
        double safetyW = 0, fatigueW = 0, stabilityW = 0;
        for (SessionReport r : reports) {
            long d = Optional.ofNullable(r.getDurationSec()).orElse(0L);
            double w = totalDuration > 0 ? (double) d / totalDuration : (1.0 / sessionsCount);
            safetyW   += w * Optional.ofNullable(r.getSafetyEmotionScore()).orElse(0);
            fatigueW  += w * Optional.ofNullable(r.getFatigueIndex()).orElse(0);
            stabilityW+= w * Optional.ofNullable(r.getStabilityIndex()).orElse(0);
        }

        // Peaks, max fatigue streak
        long peaksTotal = reports.stream().mapToLong(r -> Optional.ofNullable(r.getPeaksCount()).orElse(0)).sum();
        long maxFatigueStreak = reports.stream().mapToLong(r -> Optional.ofNullable(r.getFatigueMaxStreakSec()).orElse(0L)).max().orElse(0L);

        // Alerts aggregation (sum per type across session reports)
        Map<String, Integer> alertsByType = new HashMap<>();
        for (SessionReport r : reports) {
            Map<String, Integer> ac = Optional.ofNullable(r.getAlertsCount()).orElse(Map.of());
            for (var e : ac.entrySet()) {
                alertsByType.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }
        int alertsTotal = alertsByType.values().stream().mapToInt(Integer::intValue).sum();

        // Simple 30-day trend (optional, needs endTime)
        Double safetyLast30d = null, safetyPrev30d = null;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30 = now.minus(30, ChronoUnit.DAYS);
        LocalDateTime prev30 = now.minus(60, ChronoUnit.DAYS);

        List<SessionReport> rLast30 = reports.stream()
                .filter(r -> r.getSession().getEndTime() != null && !r.getSession().getEndTime().isBefore(last30))
                .toList();
        List<SessionReport> rPrev30 = reports.stream()
                .filter(r -> {
                    LocalDateTime end = r.getSession().getEndTime();
                    return end != null && end.isBefore(last30) && !end.isBefore(prev30);
                }).toList();

        if (!rLast30.isEmpty())  safetyLast30d = rLast30.stream().mapToInt(r -> Optional.ofNullable(r.getSafetyEmotionScore()).orElse(0)).average().orElse(0.0);
        if (!rPrev30.isEmpty())  safetyPrev30d = rPrev30.stream().mapToInt(r -> Optional.ofNullable(r.getSafetyEmotionScore()).orElse(0)).average().orElse(0.0);

        // Round small decimals for nicer JSON
        Map<String, Double> emotionRounded = weightedEmotion.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> round4(e.getValue()), (a,b)->a, LinkedHashMap::new));

        return DriverPerformanceDTO.builder()
                .driverId(driverId)
                .from(from).to(to)
                .sessionsCount(sessionsCount)
                .totalDurationSec(totalDuration)
                .avgSessionDurationSec(avgDuration)
                .latestSessionEnd(latestEnd)
                .emotionShareAvg(emotionRounded)
                .safetyAvg(round1(safetyW))
                .fatigueIndexAvg(round1(fatigueW))
                .stabilityIndexAvg(round1(stabilityW))
                .peaksTotal(peaksTotal)
                .maxFatigueStreakSec(maxFatigueStreak)
                .alertsTotal(alertsTotal)
                .alertsByType(alertsByType)
                .safetyLast30d(safetyLast30d)
                .safetyPrev30d(safetyPrev30d)
                .build();
    }

    private static double round4(double x){ return Math.round(x*10000.0)/10000.0; }
    private static double round1(double x){ return Math.round(x*10.0)/10.0; }
}
