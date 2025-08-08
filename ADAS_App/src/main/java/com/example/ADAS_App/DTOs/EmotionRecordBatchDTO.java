package com.example.ADAS_App.DTOs;

import lombok.Data;


import java.util.List;
import java.util.UUID;

@Data
public class EmotionRecordBatchDTO {
    private UUID sessionId;
    private List<EmotionRecordDTO> records;
}
