package com.example.ADAS_App.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "emotion_records")
@Data
public class EmotionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @ManyToOne
    @JoinColumn(name="session_id" )
    private Session session;

    @Column(nullable = false, length = 50)
    private String emotion; // e.g., "Fatigue", "Stress", "Anger", "Neutral"

    @Column(nullable = false)
    private double confidence; // AI prediction confidence score (0-1)

    @Column(nullable = false)
    private LocalDateTime detectedAt; // timestamp of detection
}