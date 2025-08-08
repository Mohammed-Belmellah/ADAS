package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreateSessionFromAIDTO {
    private UUID sessionId;   // Provided by AI
    private UUID driverId;    // Existing driver in DB
    private Instant startTime;
}