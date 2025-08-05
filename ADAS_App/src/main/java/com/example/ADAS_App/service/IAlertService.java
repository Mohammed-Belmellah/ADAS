package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.AlertDTO;

import java.util.List;
import java.util.UUID;

public interface IAlertService {
    AlertDTO createAlert(AlertDTO dto);
    List<AlertDTO> getAlertsByDriver(UUID driverId);
}