package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.EmotionRecordBatchDTO;
import com.example.ADAS_App.DTOs.EmotionRecordDTO;

import java.util.List;
import java.util.UUID;

public interface IEmotionRecordService {
    EmotionRecordDTO createEmotionRecord(EmotionRecordDTO dto);
    List<EmotionRecordDTO> getEmotionRecordsBySession(UUID sessionId);
    List<EmotionRecordDTO> getAllEmotionRecords();

    void deleteEmotionRecord(UUID id);
    List<EmotionRecordDTO> saveBatch(EmotionRecordBatchDTO batchDTO);
}
