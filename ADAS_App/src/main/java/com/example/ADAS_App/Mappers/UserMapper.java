package com.example.ADAS_App.Mappers;

import com.example.ADAS_App.DTOs.CreateBaseUserDTO;
import com.example.ADAS_App.DTOs.BaseUserResponseDTO;
import com.example.ADAS_App.entity.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserMapper {

    // Convert Entity → DTO
    public static BaseUserResponseDTO toDTO(User user) {
        if (user == null) return null;
        BaseUserResponseDTO dto = new BaseUserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        return dto;
    }

    // Convert DTO → Entity (basic User object)
    public static User toEntity(CreateBaseUserDTO dto, BCryptPasswordEncoder encoder, Company company) {
        if (dto == null) return null;

        Role role = dto.getRole();

        switch (role) {
            case ADMIN -> {
                Admin admin = new Admin();
                admin.setName(dto.getName());
                admin.setEmail(dto.getEmail());
                admin.setPassword(encoder.encode(dto.getPassword()));
                admin.setPhone(dto.getPhone());
                admin.setRole(Role.ADMIN);
                admin.setCompany(company);
                return admin;
            }
            case DRIVER_COMPANY -> {
                CompanyDriver driver = new CompanyDriver();
                driver.setName(dto.getName());
                driver.setEmail(dto.getEmail());
                driver.setPassword(encoder.encode(dto.getPassword()));
                driver.setPhone(dto.getPhone());
                driver.setRole(Role.DRIVER_COMPANY);
                driver.setCompany(company);
                return driver;
            }
            case DRIVER_INDEPENDENT -> {
                IndividualDriver driver = new IndividualDriver();
                driver.setName(dto.getName());
                driver.setEmail(dto.getEmail());
                driver.setPassword(encoder.encode(dto.getPassword()));
                driver.setPhone(dto.getPhone());
                driver.setRole(Role.DRIVER_INDEPENDENT);
                return driver;
            }
            default -> throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }
}