package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.IndividualDriver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IndividualDriverRepository extends JpaRepository<IndividualDriver, UUID> {
}
