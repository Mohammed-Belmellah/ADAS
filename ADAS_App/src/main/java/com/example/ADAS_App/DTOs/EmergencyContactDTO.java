package com.example.ADAS_App.DTOs;

import lombok.Data;

import java.util.UUID;

@Data
public class EmergencyContactDTO {
    private Long id;
    private UUID driverId;
    private String name;
    private String phone;
    private String relation;
}
