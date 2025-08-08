package com.example.ADAS_App.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "emotion_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="session_id" )
    private Session session;

    @ElementCollection
    @CollectionTable(name = "emotion_labels", joinColumns = @JoinColumn(name = "emotion_record_id"))
    @Column(name = "emotion")
    private List<String> emotions;

    @ElementCollection
    @CollectionTable(name = "emotion_confidences", joinColumns = @JoinColumn(name = "emotion_record_id"))
    @Column(name = "confidence")
    private List<Double> confidences;// AI prediction confidence score (0-1)

    @Column(nullable = false)
    private LocalDateTime detectedAt; // timestamp of detection
}