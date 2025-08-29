package com.example.ADAS_App.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@MappedSuperclass
public abstract class Driver extends User {

    @Column(nullable = false, length = 50)
    private String licenseNumber;

    @Column(length = 50)
    private String vehicleId;
}