package com.example.ADAS_App.Mappers;

import com.example.ADAS_App.DTOs.CompanyDriverResponseDTO;
import com.example.ADAS_App.DTOs.CreateCompanyDriverDTO;
import com.example.ADAS_App.entity.Company;
import com.example.ADAS_App.entity.CompanyDriver;

public class CompanyDriverMapper {
    public static CompanyDriverResponseDTO toDto(CompanyDriver driver) {
        if (driver == null) return null;

        CompanyDriverResponseDTO dto = new CompanyDriverResponseDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setEmail(driver.getEmail());
        dto.setPhone(driver.getPhone());
        dto.setRole(driver.getRole().name());
        dto.setCreatedAt(driver.getCreatedAt());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setVehicleId(driver.getVehicleId());

        if (driver.getCompany() != null) {
            dto.setCompanyId(driver.getCompany().getId());
        }

        return dto;
    }
    public static CompanyDriver toEntity(CreateCompanyDriverDTO dto, Company company, String encodedPassword) {
        if (dto == null) return null;

        CompanyDriver driver = new CompanyDriver();
        driver.setName(dto.getName());
        driver.setEmail(dto.getEmail());
        driver.setPhone(dto.getPhone());
        driver.setPassword(encodedPassword);
        driver.setRole(dto.getRole());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setVehicleId(dto.getVehicleId());
        driver.setCompany(company);

        return driver;
    }
}
