package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EmotionRecordDTO {

    private UUID sessionId;
    private List<String> emotions;
    private List<Double> confidences;
    private LocalDateTime detectedAt;
}