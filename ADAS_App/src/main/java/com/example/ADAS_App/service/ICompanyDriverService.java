package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CompanyDriverResponseDTO;
import com.example.ADAS_App.DTOs.CreateCompanyDriverDTO;

import java.util.List;
import java.util.UUID;

public interface ICompanyDriverService {
    CompanyDriverResponseDTO createCompanyDriver(CreateCompanyDriverDTO dto);

    CompanyDriverResponseDTO getCompanyDriverById(UUID id);

    List<CompanyDriverResponseDTO> getAllCompanyDrivers();

    CompanyDriverResponseDTO updateCompanyDriver(UUID id, CreateCompanyDriverDTO dto);

    void deleteCompanyDriver(UUID id);
}
