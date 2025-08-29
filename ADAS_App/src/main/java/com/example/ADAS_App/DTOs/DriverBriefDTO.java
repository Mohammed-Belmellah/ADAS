package com.example.ADAS_App.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverBriefDTO {
    private UUID id;
    private String fullName;   // adapt to your fields
    private String phone;
}