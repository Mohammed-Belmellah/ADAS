package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.AdminResponseDTO;
import com.example.ADAS_App.DTOs.CompanyDTO;
import com.example.ADAS_App.DTOs.CreateAdminDTO;
import com.example.ADAS_App.Mappers.AdminMapper;
import com.example.ADAS_App.entity.Admin;
import com.example.ADAS_App.entity.Company;
import com.example.ADAS_App.repository.AdminRepository;
import com.example.ADAS_App.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements IAdminService {

    private final AdminRepository adminRepo;

    private final BCryptPasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepo;

    @Override
    public AdminResponseDTO createAdmin(CreateAdminDTO dto) {
        Company company = companyRepo.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        Admin admin = AdminMapper.toEntity(dto, company, encodedPassword);
        admin = adminRepo.save(admin);
        return AdminMapper.toDto(admin);
    }

    @Override
    public AdminResponseDTO getAdminById(UUID id) {
        Admin admin = adminRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        return AdminMapper.toDto(admin);
    }

    @Override
    public List<AdminResponseDTO> getAllAdmins() {
        return adminRepo.findAll()
                .stream()
                .map(AdminMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminResponseDTO updateAdmin(UUID id, CreateAdminDTO createAdminDTO) {
        Admin admin = adminRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + id));
        Company company = companyRepo.findById(createAdminDTO.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        admin.setName(createAdminDTO.getName());
        admin.setEmail(createAdminDTO.getEmail());
        admin.setPhone(createAdminDTO.getPhone());
        admin.setDepartment(createAdminDTO.getDepartment());
        admin.setCompany(company);

        if (createAdminDTO.getPassword() != null && !createAdminDTO.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(createAdminDTO.getPassword()));
        }

        Admin updatedAdmin = adminRepo.save(admin);
        return AdminMapper.toDto(updatedAdmin);
    }

    @Override
    public void deleteAdmin(UUID id) {
        if (!adminRepo.existsById(id)) {
            throw new RuntimeException("Admin not found with ID: " + id);
        }
        adminRepo.deleteById(id);
    }
}