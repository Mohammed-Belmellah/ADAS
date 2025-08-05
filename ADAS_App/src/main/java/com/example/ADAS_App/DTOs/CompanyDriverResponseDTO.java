package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.util.UUID;

@Data
public class CompanyDriverResponseDTO extends BaseUserResponseDTO{
    private String licenseNumber;
    private String vehicleId;
    private UUID companyId;
}
