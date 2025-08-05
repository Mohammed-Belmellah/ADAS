package com.example.ADAS_App.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "company_drivers")
public class CompanyDriver extends Driver {

    @ManyToOne
    @JsonBackReference(value = "driver")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}