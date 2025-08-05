package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CompanyDriverResponseDTO;
import com.example.ADAS_App.DTOs.CreateCompanyDriverDTO;
import com.example.ADAS_App.Mappers.CompanyDriverMapper;
import com.example.ADAS_App.entity.Company;
import com.example.ADAS_App.entity.CompanyDriver;
import com.example.ADAS_App.entity.Role;
import com.example.ADAS_App.repository.CompanyDriverRepository;
import com.example.ADAS_App.repository.CompanyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyDriverServiceImpl implements ICompanyDriverService {

    private final CompanyDriverRepository companyDriverRepo;
    private final CompanyRepository companyRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CompanyDriverResponseDTO createCompanyDriver(CreateCompanyDriverDTO dto) {
        Company company = companyRepo.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanyDriver companyDriver = CompanyDriverMapper.toEntity(
                dto,
                company,
                passwordEncoder.encode(dto.getPassword())
        );
        companyDriver.setRole(Role.DRIVER_COMPANY);

        return CompanyDriverMapper.toDto(companyDriverRepo.save(companyDriver));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDriverResponseDTO getCompanyDriverById(UUID id) {
        return companyDriverRepo.findById(id)
                .map(CompanyDriverMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Company driver not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDriverResponseDTO> getAllCompanyDrivers() {
        return companyDriverRepo.findAll()
                .stream()
                .map(CompanyDriverMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompanyDriverResponseDTO updateCompanyDriver(UUID id, CreateCompanyDriverDTO dto) {
        CompanyDriver existingDriver = companyDriverRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Company driver not found"));

        Company company = companyRepo.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        existingDriver.setName(dto.getName());
        existingDriver.setEmail(dto.getEmail());
        existingDriver.setPhone(dto.getPhone());
        existingDriver.setPassword(passwordEncoder.encode(dto.getPassword()));
        existingDriver.setCompany(company);
        return CompanyDriverMapper.toDto(companyDriverRepo.save(existingDriver));
    }

    @Override
    public void deleteCompanyDriver(UUID id) {
        companyDriverRepo.deleteById(id);
    }
}
