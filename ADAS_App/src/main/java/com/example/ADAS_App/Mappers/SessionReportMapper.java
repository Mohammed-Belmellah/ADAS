package com.example.ADAS_App.Mappers;

import com.example.ADAS_App.DTOs.SessionReportDTO;
import com.example.ADAS_App.entity.SessionReport;

public final class SessionReportMapper {

    public static SessionReportDTO toDTO(SessionReport r) {
        SessionReportDTO dto = new SessionReportDTO();
        dto.setSessionId(r.getSession().getId());
        dto.setStartTime(r.getSession().getStartTime());
        dto.setEndTime(r.getSession().getEndTime());

        dto.setDurationSec(r.getDurationSec());
        dto.setEmotionShare(r.getEmotionShare());
        dto.setConfAvg(r.getConfAvg());
        dto.setDominantEmotion(r.getDominantEmotion());

        dto.setFatigueCumSec(r.getFatigueCumSec());
        dto.setFatigueMaxStreakSec(r.getFatigueMaxStreakSec());
        dto.setStressCumSec(r.getStressCumSec());
        dto.setAngerCumSec(r.getAngerCumSec());
        dto.setPeaksCount(r.getPeaksCount());

        dto.setSafetyEmotionScore(r.getSafetyEmotionScore());
        dto.setFatigueIndex(r.getFatigueIndex());
        dto.setStabilityIndex(r.getStabilityIndex());

        dto.setAlertsCount(r.getAlertsCount());
        dto.setTotalAlerts(r.getTotalAlerts());
        dto.setUnresolvedAlerts(r.getUnresolvedAlerts());

        return dto;
    }
}

