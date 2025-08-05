package com.example.ADAS_App.repository;


import com.example.ADAS_App.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
    List<Admin> findByCompanyId(UUID companyId);
}