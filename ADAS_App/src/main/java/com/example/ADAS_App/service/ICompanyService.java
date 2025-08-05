package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CompanyDTO;

import java.util.List;
import java.util.UUID;

public interface ICompanyService {
    CompanyDTO createCompany(CompanyDTO dto);
    List<CompanyDTO> getAllCompanies();
    CompanyDTO getCompanyById(UUID id);
}
