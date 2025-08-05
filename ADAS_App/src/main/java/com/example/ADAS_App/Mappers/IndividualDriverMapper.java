package com.example.ADAS_App.Mappers;

import com.example.ADAS_App.DTOs.CreateIndividualDriverDTO;
import com.example.ADAS_App.DTOs.IndividualDriverResponseDTO;
import com.example.ADAS_App.entity.IndividualDriver;
import com.example.ADAS_App.entity.Role;

public class IndividualDriverMapper {

    public static IndividualDriverResponseDTO toDto(IndividualDriver driver) {
        if (driver == null) return null;

        IndividualDriverResponseDTO dto = new IndividualDriverResponseDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setEmail(driver.getEmail());
        dto.setPhone(driver.getPhone());
        dto.setRole(driver.getRole().name());
        dto.setCreatedAt(driver.getCreatedAt());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setVehicleId(driver.getVehicleId());
        return dto;
    }
    public static IndividualDriver toEntity(CreateIndividualDriverDTO dto, String encodedPassword) {
        if (dto == null) return null;

        IndividualDriver driver = new IndividualDriver();
        driver.setName(dto.getName());
        driver.setEmail(dto.getEmail());
        driver.setPhone(dto.getPhone());
        driver.setPassword(encodedPassword);
        driver.setRole(Role.DRIVER_INDEPENDENT);
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setVehicleId(dto.getVehicleId());

        return driver;
    }
}
