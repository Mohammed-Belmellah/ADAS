package com.example.ADAS_App.service;

import com.example.ADAS_App.entity.EmotionRecord;
import com.example.ADAS_App.entity.EmotionType;
import com.example.ADAS_App.entity.Session;
import com.example.ADAS_App.repository.AlertRepository;
import com.example.ADAS_App.repository.EmotionRecordRepository;
import com.example.ADAS_App.util.EmotionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertEngine {

    private final EmotionRecordRepository recordRepo;
    private final IAlertService alertService;
    private final AlertRepository alertRepo;

    // thresholds (tune as needed)
    private static final double TIRED_TH = 0.6;     // strong tired
    private static final int TIRED_STREAK_SEC = 0; // 10 min continuous
    private static final double ANGRY_PEAK_TH = 0.6;
    private static final int PEAK_MIN_DURATION_SEC = 1;
    private static final int PEAKS_BURST_COUNT = 1;  // e.g., 3 strong peaks in a short time

    @Transactional
    public void evaluateAndBroadcast(Session session, List<EmotionRecord> newRecords) {
        if (newRecords == null || newRecords.isEmpty()) return;

        // Pull one record before the batch to stitch streaks correctly
        var first = newRecords.getFirst();
        EmotionRecord prev = recordRepo
                .findTop1BySession_IdAndDetectedAtBeforeOrderByDetectedAtDesc(
                        session.getId(), first.getDetectedAt())
                .orElse(null);

        List<EmotionRecord> window = new ArrayList<>();
        if (prev != null) window.add(prev);
        window.addAll(newRecords);

        // Walk intervals and detect conditions
        long tiredStreak = 0;
        int strongPeaks = 0;

        for (int i = 0; i < window.size(); i++) {
            EmotionRecord cur = window.get(i);
            LocalDateTime from = cur.getDetectedAt();
            LocalDateTime to = (i < window.size() - 1) ? window.get(i+1).getDetectedAt() : from; // last interval length 0
            long dt = Math.max(0, Duration.between(from, to).getSeconds());

            // dominant of this record
            int idx = idxMax(cur.getConfidences());
            String label = cur.getEmotions().get(idx);
            var emo = EmotionUtils.parseEmotion(label);
            double conf = cur.getConfidences().get(idx);

            // --- Tired streak ---
            if (emo == EmotionType.TIRED && conf >= TIRED_TH) {
                tiredStreak += dt;
            } else {
                tiredStreak = 0;
            }

            // --- Angry peak detection ---
            if (emo == EmotionType.ANGRY && conf >= ANGRY_PEAK_TH && dt >= PEAK_MIN_DURATION_SEC) {
                strongPeaks++;
            }
        }

        // Cooldown example: avoid spamming same alert type within the last 15 minutes
        LocalDateTime cooldownAfter = LocalDateTime.now().minusMinutes(15);

        if (tiredStreak >= TIRED_STREAK_SEC &&
                alertServiceExists(session.getId(), "EXTREME_FATIGUE", cooldownAfter)) {
            alertService.createAndBroadcast(session, "EXTREME_FATIGUE",
                    "Continuous tired state ≥ 10 minutes detected.");
        }

        if (strongPeaks >= PEAKS_BURST_COUNT &&
                alertServiceExists(session.getId(), "FREQUENT_PEAKS", cooldownAfter)) {
            alertService.createAndBroadcast(session, "FREQUENT_PEAKS",
                    "Multiple high-intensity emotional peaks detected.");
        }
    }

    // helper
    private boolean alertServiceExists(UUID sessionId, String type, LocalDateTime after) {
        return !alertRepo.existsBySession_IdAndTypeAndCreatedAtAfter(sessionId, type, after);
    }

    private static int idxMax(List<Double> xs) {
        int idx = 0; double best = -1;
        for (int i=0;i<xs.size();i++){ Double v=xs.get(i); if(v!=null && v>best){best=v; idx=i;} }
        return idx;
    }
}

