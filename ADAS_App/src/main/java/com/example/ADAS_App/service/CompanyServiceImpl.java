package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CompanyDTO;
import com.example.ADAS_App.Mappers.CompanyMapper;
import com.example.ADAS_App.entity.Company;
import com.example.ADAS_App.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyServiceImpl implements ICompanyService{

    private final CompanyRepository companyRepo;

    public CompanyServiceImpl(CompanyRepository companyRepo) {
        this.companyRepo = companyRepo;
    }

    public CompanyDTO createCompany(CompanyDTO dto) {
        Company company = CompanyMapper.toEntity(dto);
        Company saved = companyRepo.save(company);
        return CompanyMapper.toDTO(saved);
    }

    public List<CompanyDTO> getAllCompanies() {
        return companyRepo.findAll()
                .stream()
                .map(CompanyMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CompanyDTO getCompanyById(UUID id) {
        return companyRepo.findById(id)
                .map(CompanyMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }
}