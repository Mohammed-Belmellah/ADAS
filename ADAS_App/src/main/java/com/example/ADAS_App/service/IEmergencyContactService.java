package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.EmergencyContactDTO;

import java.util.List;
import java.util.UUID;

public interface IEmergencyContactService {
    EmergencyContactDTO addContact(EmergencyContactDTO dto);
    List<EmergencyContactDTO> getContactsByDriverId(UUID driverId);
}