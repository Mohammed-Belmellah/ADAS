package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.AlertDTO;
import com.example.ADAS_App.Mappers.AlertMapper;
import com.example.ADAS_App.entity.Alert;
import com.example.ADAS_App.entity.User;
import com.example.ADAS_App.repository.AlertRepository;
import com.example.ADAS_App.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertServiceImpl implements IAlertService {

    private final AlertRepository alertRepo;
    private final UserRepository userRepo;

    public AlertServiceImpl(AlertRepository alertRepo, UserRepository userRepo) {
        this.alertRepo = alertRepo;
        this.userRepo = userRepo;
    }

    public AlertDTO createAlert(AlertDTO dto) {
        User driver = userRepo.findById(dto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Alert alert = new Alert();
        alert.setDriver(driver);
        alert.setType(dto.getType());
        alert.setMessage(dto.getMessage());
        alert.setResolved(dto.isResolved());
        alert.setCreatedAt(dto.getCreatedAt());

        return AlertMapper.toDTO(alertRepo.save(alert));
    }

    public List<AlertDTO> getAlertsByDriver(UUID driverId) {
        return alertRepo.findByDriverId(driverId)
                .stream()
                .map(AlertMapper::toDTO)
                .collect(Collectors.toList());
    }
}
