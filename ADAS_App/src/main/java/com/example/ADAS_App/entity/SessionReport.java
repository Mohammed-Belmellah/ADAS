package com.example.ADAS_App.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "session_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    private Long durationSec;

    // Parts du temps par émotion: {"calm":0.54,"stress":0.22,...}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> emotionShare;

    // Confiance moyenne par émotion
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> confAvg;

    private String dominantEmotion;

    // Cumuls / streaks
    private Long fatigueCumSec;
    private Long fatigueMaxStreakSec;
    private Long stressCumSec;
    private Long angerCumSec;
    private Integer peaksCount;

    // Scores 0–100
    private Integer safetyEmotionScore;
    private Integer fatigueIndex;
    private Integer stabilityIndex;

    // Option: compteurs d’alertes de la session
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Integer> alertsCount;

    private Integer totalAlerts;
    private Integer unresolvedAlerts;
}
