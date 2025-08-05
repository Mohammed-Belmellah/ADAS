package com.example.ADAS_App.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCompanyDriverDTO extends CreateBaseUserDTO {
    @NotNull
    private String licenseNumber;

    @NotNull
    private String vehicleId;

    @NotNull
    private UUID companyId;

}
