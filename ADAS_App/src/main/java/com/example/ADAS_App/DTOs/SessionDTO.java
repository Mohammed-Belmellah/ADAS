package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SessionDTO {
    private UUID id;
    private UUID driverId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String averageEmotion;
}
