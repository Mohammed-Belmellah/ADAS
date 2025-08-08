package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.time.Instant;

@Data
public class EndSessionDTO {
    private Instant endTime;
}
