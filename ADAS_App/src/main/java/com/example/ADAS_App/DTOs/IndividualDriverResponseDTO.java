package com.example.ADAS_App.DTOs;

import lombok.Data;

@Data
public class IndividualDriverResponseDTO extends BaseUserResponseDTO{
    private String licenseNumber;
    private String vehicleId;
}
