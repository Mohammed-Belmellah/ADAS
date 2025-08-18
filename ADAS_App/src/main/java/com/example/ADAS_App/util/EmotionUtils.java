package com.example.ADAS_App.util;

import com.example.ADAS_App.entity.EmotionType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class EmotionUtils {
    private EmotionUtils() {}

    public static final Map<EmotionType, Double> RISK_WEIGHT = Map.of(
            EmotionType.HAPPY,0.0, EmotionType.NEUTRAL,0.0,
            EmotionType.SAD,0.5, EmotionType.ANGRY,0.8,
            EmotionType.SURPRISED,0.3, EmotionType.TIRED,1.0
    );

    public static final Map<EmotionType, Double> STRONG_TH = Map.of(
            EmotionType.SAD,0.6, EmotionType.ANGRY,0.6,
            EmotionType.SURPRISED,0.7, EmotionType.TIRED,0.6
    );



    public static EmotionType parseEmotion(String label) {
        if (label == null) return EmotionType.NEUTRAL;
        switch (label.trim().toLowerCase()) {
            case "happy": return EmotionType.HAPPY;
            case "neutral": case "calm": return EmotionType.NEUTRAL;
            case "sad": return EmotionType.SAD;
            case "angry": case "anger": case "stress": return EmotionType.ANGRY; // ajuste si besoin
            case "surprised": case "surprise": return EmotionType.SURPRISED;
            case "tired": case "fatigue": return EmotionType.TIRED;
            default: return EmotionType.NEUTRAL;
        }
    }

    /** Normalize to canonical lowercase labels matching your 6 emotions */
    public static List<String> normalizeLabels(List<String> labels) {
        if (labels == null) return List.of();
        return labels.stream()
                .map(EmotionUtils::parseEmotion)            // enum canonicalization
                .map(e -> e.name().toLowerCase())          // "HAPPY" -> "happy"
                .collect(Collectors.toList());
    }
}