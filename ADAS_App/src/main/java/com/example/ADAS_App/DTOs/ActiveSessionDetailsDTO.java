package com.example.ADAS_App.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionDetailsDTO {
    private UUID sessionId;
    private LocalDateTime startTime;
    private String averageEmotion;     // optional
    private DriverBriefDTO driver;

    private Integer unresolvedAlertsCount;
    private List<AlertDTO> alerts;     // you can limit to recent X if desired
}
