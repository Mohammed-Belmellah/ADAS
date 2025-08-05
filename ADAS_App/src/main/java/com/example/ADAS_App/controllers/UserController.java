package com.example.ADAS_App.controllers;

import com.example.ADAS_App.DTOs.CreateBaseUserDTO;
import com.example.ADAS_App.DTOs.BaseUserResponseDTO;
import com.example.ADAS_App.service.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }



    @GetMapping
    public ResponseEntity<List<BaseUserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseUserResponseDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
