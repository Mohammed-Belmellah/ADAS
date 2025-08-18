package com.example.ADAS_App.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndSessionResponse {
    private SessionDTO session;
    private SessionReportDTO report;
}

