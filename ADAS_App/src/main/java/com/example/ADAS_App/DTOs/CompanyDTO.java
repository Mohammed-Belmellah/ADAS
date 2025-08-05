package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.util.UUID;

@Data
public class CompanyDTO {
    private UUID id;
    private String name;
    private String industry;
    private String address;
}
