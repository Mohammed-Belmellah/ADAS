package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.util.UUID;
@Data
public class AdminResponseDTO extends BaseUserResponseDTO {
    private String department;
    private UUID companyId;
}
