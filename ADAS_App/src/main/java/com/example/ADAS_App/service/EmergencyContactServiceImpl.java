package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.EmergencyContactDTO;
import com.example.ADAS_App.Mappers.EmergencyContactMapper;
import com.example.ADAS_App.entity.EmergencyContact;
import com.example.ADAS_App.entity.IndividualDriver;
import com.example.ADAS_App.repository.EmergencyContactRepository;
import com.example.ADAS_App.repository.IndividualDriverRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmergencyContactServiceImpl implements IEmergencyContactService {

    private final EmergencyContactRepository contactRepo;
    private final IndividualDriverRepository driverRepo;

    public EmergencyContactServiceImpl(EmergencyContactRepository contactRepo,
                                   IndividualDriverRepository driverRepo) {
        this.contactRepo = contactRepo;
        this.driverRepo = driverRepo;
    }

    public EmergencyContactDTO addContact(EmergencyContactDTO dto) {
        IndividualDriver driver = driverRepo.findById(dto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        EmergencyContact contact = EmergencyContactMapper.toEntity(dto, driver);
        EmergencyContact saved = contactRepo.save(contact);
        return EmergencyContactMapper.toDTO(saved);
    }

    public List<EmergencyContactDTO> getContactsByDriverId(java.util.UUID driverId) {
        return contactRepo.findByDriverId(driverId)
                .stream()
                .map(EmergencyContactMapper::toDTO)
                .collect(Collectors.toList());
    }
}
