package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AlertDTO {
    private UUID id;
    private UUID driverId;
    private String type;
    private String message;
    private boolean resolved;
    private LocalDateTime createdAt;
}