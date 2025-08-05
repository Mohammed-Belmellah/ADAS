package com.example.ADAS_App.DTOs;

import com.example.ADAS_App.entity.Company;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateAdminDTO extends CreateBaseUserDTO {
    private String department;
    private UUID companyid;
}
