package com.example.ADAS_App.repository;

import com.example.ADAS_App.entity.CompanyDriver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyDriverRepository extends JpaRepository<CompanyDriver, UUID> {
    List<CompanyDriver> findByCompanyId(UUID companyId);
}