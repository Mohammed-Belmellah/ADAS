package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.AlertDTO;
import com.example.ADAS_App.entity.Alert;
import com.example.ADAS_App.entity.Session;

import java.util.List;
import java.util.UUID;

public interface IAlertService {
    AlertDTO createAlert(AlertDTO dto);
    List<AlertDTO> getAlertsByDriver(UUID driverId);
    List<Alert> getAlertsBySession(UUID sessionId);
    Alert createAndBroadcast(Session session, String type, String message);
    List<AlertDTO> listBySession(UUID sessionId);
    AlertDTO markResolved(UUID alertId);
}