package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.AdminResponseDTO;
import com.example.ADAS_App.DTOs.CreateAdminDTO;

import java.util.List;
import java.util.UUID;

public interface IAdminService {
    AdminResponseDTO createAdmin(CreateAdminDTO createAdminDTO);

    AdminResponseDTO getAdminById(UUID id);

    List<AdminResponseDTO> getAllAdmins();

    AdminResponseDTO updateAdmin(UUID id, CreateAdminDTO createAdminDTO);

    void deleteAdmin(UUID id);
}
