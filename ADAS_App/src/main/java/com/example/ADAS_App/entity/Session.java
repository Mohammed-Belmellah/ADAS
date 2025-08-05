package com.example.ADAS_App.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Data
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;  // can be CompanyDriver or IndividualDriver

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime; // nullable if session is ongoing

    @Column(length = 50)
    private String averageEmotion; // computed after session ends
}
