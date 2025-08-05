package com.example.ADAS_App.Mappers;

import com.example.ADAS_App.DTOs.SessionDTO;
import com.example.ADAS_App.entity.Session;

public class SessionMapper {

    public static SessionDTO toDTO(Session session) {
        if (session == null) return null;
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setDriverId(session.getDriver().getId());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setAverageEmotion(session.getAverageEmotion());
        return dto;
    }
}
