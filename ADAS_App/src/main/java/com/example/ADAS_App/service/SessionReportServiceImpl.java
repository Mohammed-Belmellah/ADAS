package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.SessionReportDTO;
import com.example.ADAS_App.Mappers.SessionReportMapper;
import com.example.ADAS_App.entity.*;
import com.example.ADAS_App.repository.AlertRepository;
import com.example.ADAS_App.repository.EmotionRecordRepository;
import com.example.ADAS_App.repository.SessionReportRepository;
import com.example.ADAS_App.repository.SessionRepository;
import com.example.ADAS_App.util.EmotionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionReportServiceImpl implements ISessionReportService  {

    private final SessionRepository sessionRepo;
    private final EmotionRecordRepository recordRepo;
    private final SessionReportRepository reportRepo;
    private final AlertRepository alertRepo;

    /**
     * Compute & persist a report for a finished session.
     */
    public SessionReportDTO generate(UUID sessionId) {
        Session s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        if (s.getEndTime() == null)
            throw new IllegalStateException("Session not ended yet");

        List<EmotionRecord> records = recordRepo.findBySession_IdOrderByDetectedAtAsc(sessionId);
        long durationSec = duration(s.getStartTime(), s.getEndTime());

        List<Alert> alerts = alertRepo.findBySession_Id(sessionId);

        Map<String, Integer> byType = alerts.stream()
                .collect(Collectors.groupingBy(Alert::getType, Collectors.summingInt(a -> 1)));

        int totalAlerts = alerts.size();
        int unresolved = (int) alerts.stream().filter(a -> !a.isResolved()).count();

        if (records.isEmpty()) {
            SessionReport empty = SessionReport.builder()
                    .session(s).durationSec(durationSec)
                    .emotionShare(zeroShares())
                    .confAvg(zeroShares())
                    .dominantEmotion(null)
                    .fatigueCumSec(0L).fatigueMaxStreakSec(0L)
                    .stressCumSec(0L).angerCumSec(0L)
                    .peaksCount(0)
                    .safetyEmotionScore(100).fatigueIndex(100).stabilityIndex(100)
                    .alertsCount(byType)
                    .totalAlerts(totalAlerts)
                    .unresolvedAlerts(unresolved)
                    .build();
            return SessionReportMapper.toDTO(reportRepo.save(empty));
        }

        // Accumulators
        Map<EmotionType, Long> durBy = new EnumMap<>(EmotionType.class);
        Map<EmotionType, DoubleSummaryStatistics> confBy = new EnumMap<>(EmotionType.class);

        long tiredCum = 0, tiredStreak = 0, tiredMax = 0;
        long stressCum = 0, angerCum = 0;
        int peaks = 0;

        for (int i = 0; i < records.size(); i++) {
            EmotionRecord cur = records.get(i);

            // Skip malformed records
            if (cur.getEmotions() == null || cur.getConfidences() == null
                    || cur.getEmotions().isEmpty()
                    || cur.getConfidences().isEmpty()
                    || cur.getEmotions().size() != cur.getConfidences().size()) {
                continue;
            }

            LocalDateTime from = cur.getDetectedAt();
            LocalDateTime to = (i < records.size() - 1) ? records.get(i + 1).getDetectedAt() : s.getEndTime();
            long dt = Math.max(0, duration(from, to));
            if (dt == 0) continue;

            int idx = idxMax(cur.getConfidences());
            EmotionType emo = EmotionUtils.parseEmotion(cur.getEmotions().get(idx));
            double conf = cur.getConfidences().get(idx);

            durBy.merge(emo, dt, Long::sum);
            confBy.computeIfAbsent(emo, k -> new DoubleSummaryStatistics()).accept(conf);

            // Fatigue streak & cumulative
            double thTired = EmotionUtils.STRONG_TH.getOrDefault(EmotionType.TIRED, 0.6);
            if (emo == EmotionType.TIRED && conf >= thTired) {
                tiredCum += dt;
                tiredStreak += dt;
                tiredMax = Math.max(tiredMax, tiredStreak);
            } else if (emo != EmotionType.TIRED) {
                tiredStreak = 0;
            }

            // Angry / stress mapping
            double thAngry = EmotionUtils.STRONG_TH.getOrDefault(EmotionType.ANGRY, 0.6);
            if (emo == EmotionType.ANGRY && conf >= thAngry) {
                angerCum += dt;
                stressCum += dt;
            }

            // Peaks
            if (conf >= 0.8 &&
                    (emo == EmotionType.ANGRY || emo == EmotionType.TIRED || emo == EmotionType.SURPRISED) &&
                    dt >= 5) {
                peaks++;
            }
        }

        // Shares & averages
        Map<String, Double> emotionShare = toShare(durBy, durationSec);
        Map<String, Double> confAvg = confBy.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name().toLowerCase(), e -> r4(e.getValue().getAverage())));
        String dominant = emotionShare.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

        // Scores
        int safety = safetyScoreWeighted(durBy, durationSec);
        int fatigueIdx = fatigueIndex(tiredCum, tiredMax);
        int stability = stabilityIndex(emotionShare);

        // Update session average emotion
        s.setAverageEmotion(dominant);
        sessionRepo.save(s);

        SessionReport report = SessionReport.builder()
                .session(s)
                .durationSec(durationSec)
                .emotionShare(emotionShare)
                .confAvg(confAvg)
                .dominantEmotion(dominant)
                .fatigueCumSec(tiredCum)
                .fatigueMaxStreakSec(tiredMax)
                .stressCumSec(stressCum)
                .angerCumSec(angerCum)
                .peaksCount(peaks)
                .safetyEmotionScore(safety)
                .fatigueIndex(fatigueIdx)
                .stabilityIndex(stability)
                .alertsCount(byType)          // <<< use your computed map
                .totalAlerts(totalAlerts)     // <<< set totals
                .unresolvedAlerts(unresolved) // <<< set unresolved
                .build();
        return SessionReportMapper.toDTO(reportRepo.save(report));
    }

    // ===== Helpers =====

    private static long duration(LocalDateTime from, LocalDateTime to) {
        return Math.max(0, Duration.between(from, to).getSeconds());
    }

    private static int idxMax(List<Double> xs) {
        int idx = 0; double best = -1;
        for (int i = 0; i < xs.size(); i++) {
            Double v = xs.get(i);
            if (v != null && v > best) { best = v; idx = i; }
        }
        return idx;
    }

    private static Map<String, Double> toShare(Map<EmotionType, Long> durBy, long totalSec) {
        Map<String, Double> out = new LinkedHashMap<>();
        if (totalSec <= 0) return zeroShares();
        for (EmotionType e : EmotionType.values()) {
            long v = durBy.getOrDefault(e, 0L);
            out.put(e.name().toLowerCase(), r4(v / (double) totalSec));
        }
        return out;
    }

    private static Map<String, Double> zeroShares() {
        Map<String, Double> zeros = new LinkedHashMap<>();
        for (EmotionType e : EmotionType.values()) {
            zeros.put(e.name().toLowerCase(), 0.0);
        }
        return zeros;
    }

    /** Round to 4 decimals */
    private static double r4(double x) {
        return Math.round(x * 10000.0) / 10000.0;
    }

    private static int safetyScoreWeighted(Map<EmotionType, Long> durBy, long totalSec) {
        if (totalSec <= 0) return 100;
        double risk = 0.0;
        for (var e : durBy.entrySet()) {
            double frac = e.getValue() / (double) totalSec;
            double w = EmotionUtils.RISK_WEIGHT.getOrDefault(e.getKey(), 0.0);
            risk += frac * w;
        }
        risk = Math.min(1.0, Math.max(0.0, risk));
        return (int) Math.round(100.0 * (1.0 - risk));
    }

    private static int fatigueIndex(long tiredCumSec, long tiredMaxStreakSec) {
        double score = 100.0
                - Math.min(100.0, (tiredCumSec / 60.0) * 2.0)
                - Math.min(40.0, (tiredMaxStreakSec / 60.0) * 4.0);
        return (int) Math.max(0, Math.round(score));
    }

    private static int stabilityIndex(Map<String, Double> share) {
        double calm = share.getOrDefault("neutral", 0.0) + 0.5 * share.getOrDefault("happy", 0.0);
        double neg  = share.getOrDefault("tired", 0.0)
                + share.getOrDefault("angry", 0.0)
                + share.getOrDefault("sad", 0.0)
                + 0.5 * share.getOrDefault("surprised", 0.0);
        double sc = 50.0 + 50.0 * calm - 30.0 * neg;
        return (int) Math.max(0, Math.min(100, Math.round(sc)));
    }
    @Override
    @Transactional(readOnly = true)
    public SessionReportDTO getBySessionId(UUID sessionId) {
        SessionReport r = reportRepo.findBySession_Id(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session report not found for session: " + sessionId));
        return SessionReportMapper.toDTO(r);
    }
}
