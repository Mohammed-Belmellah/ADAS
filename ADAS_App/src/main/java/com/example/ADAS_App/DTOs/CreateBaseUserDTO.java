package com.example.ADAS_App.DTOs;

import com.example.ADAS_App.entity.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBaseUserDTO {
    private String name;
    private String email;
    private String password;
    private String phone;
    private Role role;      // ADMIN / DRIVER_COMPANY / DRIVER_INDEPENDENT
    private UUID companyId;   // Optional: only for admins or company drivers
}