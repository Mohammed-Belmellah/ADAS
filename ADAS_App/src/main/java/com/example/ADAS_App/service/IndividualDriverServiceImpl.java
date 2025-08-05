package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CreateIndividualDriverDTO;
import com.example.ADAS_App.DTOs.IndividualDriverResponseDTO;
import com.example.ADAS_App.Mappers.IndividualDriverMapper;
import com.example.ADAS_App.entity.IndividualDriver;
import com.example.ADAS_App.entity.Role;
import com.example.ADAS_App.repository.IndividualDriverRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IndividualDriverServiceImpl implements IIndividualDriverService {

    private final IndividualDriverRepository individualDriverRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IndividualDriverResponseDTO createIndividualDriver(CreateIndividualDriverDTO dto) {
        IndividualDriver driver = IndividualDriverMapper.toEntity(dto, passwordEncoder.encode(dto.getPassword()));
        driver.setRole(Role.DRIVER_INDEPENDENT);

        IndividualDriver savedDriver = individualDriverRepo.save(driver);
        return IndividualDriverMapper.toDto(savedDriver);
    }

    @Override
    public List<IndividualDriverResponseDTO> getAllIndividualDrivers() {
        return individualDriverRepo.findAll()
                .stream()
                .map(IndividualDriverMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public IndividualDriverResponseDTO getIndividualDriverById(UUID id) {
        return individualDriverRepo.findById(id)
                .map(IndividualDriverMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Individual driver not found"));
    }

    @Override
    public IndividualDriverResponseDTO updateIndividualDriver(UUID id, CreateIndividualDriverDTO dto) {
        IndividualDriver driver = individualDriverRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Individual driver not found"));

        driver.setName(dto.getName());
        driver.setEmail(dto.getEmail());
        driver.setPhone(dto.getPhone());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setVehicleId(dto.getVehicleId());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            driver.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        IndividualDriver updatedDriver = individualDriverRepo.save(driver);
        return IndividualDriverMapper.toDto(updatedDriver);
    }

    @Override
    public void deleteIndividualDriver(UUID id) {
        individualDriverRepo.deleteById(id);
    }
}