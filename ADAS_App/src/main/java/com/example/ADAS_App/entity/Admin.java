package com.example.ADAS_App.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "admins")
public class Admin extends User {

    @ManyToOne
    @JoinColumn(name = "company_id" , nullable = false)
    @JsonBackReference(value = "admin")
    private Company company;  // Admin manages one company

    @Column(length = 100)
    private String department;

}
