package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BaseUserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}