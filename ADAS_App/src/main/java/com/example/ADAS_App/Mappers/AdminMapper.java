package com.example.ADAS_App.Mappers;

import com.example.ADAS_App.DTOs.AdminResponseDTO;
import com.example.ADAS_App.DTOs.CreateAdminDTO;
import com.example.ADAS_App.entity.Admin;
import com.example.ADAS_App.entity.Company;
import com.example.ADAS_App.entity.Role;
import com.example.ADAS_App.entity.User;

public class AdminMapper {
    public static AdminResponseDTO toDto(Admin admin) {
        if (admin == null) return null;

        AdminResponseDTO dto = new AdminResponseDTO();
        dto.setId(admin.getId());
        dto.setName(admin.getName());
        dto.setEmail(admin.getEmail());
        dto.setPhone(admin.getPhone());
        dto.setRole(admin.getRole().name());
        dto.setCreatedAt(admin.getCreatedAt());

        if (admin.getCompany() != null) {
            dto.setCompanyId(admin.getCompany().getId());
        }

        dto.setDepartment(admin.getDepartment());

        return dto;
    }
    public static Admin toEntity(CreateAdminDTO dto, Company company, String encodedPassword) {
        if (dto == null) return null;

        Admin admin = new Admin();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setPhone(dto.getPhone());
        admin.setPassword(encodedPassword);
        admin.setRole(Role.ADMIN);
        admin.setDepartment(dto.getDepartment());
        admin.setCompany(company);

        return admin;
    }
}
