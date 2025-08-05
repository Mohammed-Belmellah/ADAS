package com.example.ADAS_App.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateIndividualDriverDTO extends CreateBaseUserDTO{
    @NotNull
    private String licenseNumber;

    @NotNull
    private String vehicleId;
}
