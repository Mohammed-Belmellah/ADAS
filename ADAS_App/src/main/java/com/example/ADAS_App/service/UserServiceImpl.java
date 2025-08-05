package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CreateBaseUserDTO;
import com.example.ADAS_App.DTOs.BaseUserResponseDTO;
import com.example.ADAS_App.Mappers.UserMapper;
import com.example.ADAS_App.repository.CompanyRepository;
import com.example.ADAS_App.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepo;
    private final CompanyRepository companyRepo;
    private final BCryptPasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepo, CompanyRepository companyRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.companyRepo = companyRepo;
        this.encoder = encoder;
    }


    @Override
    public void deleteUser(UUID id) {
        if (!userRepo.existsById(id)) {
            throw new RuntimeException("Admin not found with ID: " + id);
        }
        userRepo.deleteById(id);

    }

    @Override
    public List<BaseUserResponseDTO> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    @Override
    public BaseUserResponseDTO getUserById(UUID id) {
        return userRepo.findById(id)
                .map(UserMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
