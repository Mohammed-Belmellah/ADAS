package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CreateBaseUserDTO;
import com.example.ADAS_App.DTOs.BaseUserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    void deleteUser(UUID id);
    List<BaseUserResponseDTO> getAllUsers();
    BaseUserResponseDTO getUserById(UUID id);
}