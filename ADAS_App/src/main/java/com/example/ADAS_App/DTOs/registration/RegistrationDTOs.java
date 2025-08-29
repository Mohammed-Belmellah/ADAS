package com.example.ADAS_App.DTOs.registration;

import java.util.UUID;

public class RegistrationDTOs {
    // src/main/java/.../dto/registration/RegistrationDTOs.java
    public record CompanyAdminSignup(
            String email,
            String password,
            String name,
            String companyName

    ) {}

    public record CompanyDriverSignup(
            String email,
            String password,
            String name,
            String licenseNumber,
            UUID companyId
    ) {}

    public record IndividualDriverSignup(
            String email,
            String password,
            String licenseNumber,
            String name

    ) {}
}
