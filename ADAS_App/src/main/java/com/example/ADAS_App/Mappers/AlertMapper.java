package com.example.ADAS_App.Mappers;

import com.example.ADAS_App.DTOs.AlertDTO;
import com.example.ADAS_App.entity.Alert;

public class AlertMapper {

    public static AlertDTO toDTO(Alert alert) {
        if (alert == null) return null;
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setDriverId(alert.getDriver().getId());
        dto.setSessionId(alert.getSession().getId());
        dto.setType(alert.getType());
        dto.setMessage(alert.getMessage());
        dto.setResolved(alert.isResolved());
        dto.setCreatedAt(alert.getCreatedAt());
        return dto;
    }

}
