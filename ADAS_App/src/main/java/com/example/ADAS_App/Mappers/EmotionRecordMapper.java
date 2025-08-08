package com.example.ADAS_App.Mappers;


import com.example.ADAS_App.DTOs.EmotionRecordDTO;
import com.example.ADAS_App.entity.EmotionRecord;
import com.example.ADAS_App.entity.Session;

public class EmotionRecordMapper {

    public static EmotionRecordDTO toDTO(EmotionRecord record) {
        if (record == null) return null;
        EmotionRecordDTO dto = new EmotionRecordDTO();
        dto.setSessionId(record.getSession().getId());
        dto.setEmotions(record.getEmotions());
        dto.setConfidences(record.getConfidences());
        dto.setDetectedAt(record.getDetectedAt());
        return dto;
    }
    public static EmotionRecord toEntity(EmotionRecordDTO dto, Session session) {
        if (dto == null || session == null) return null;

        EmotionRecord entity = new EmotionRecord();
        entity.setEmotions(dto.getEmotions());
        entity.setConfidences(dto.getConfidences());
        entity.setDetectedAt(dto.getDetectedAt());
        entity.setSession(session);

        return entity;
    }
}