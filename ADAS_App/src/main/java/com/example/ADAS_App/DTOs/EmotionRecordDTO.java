package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EmotionRecordDTO {
    private UUID id;
    private UUID sessionId;
    private String emotion;
    private double confidence;
    private LocalDateTime detectedAt;
}