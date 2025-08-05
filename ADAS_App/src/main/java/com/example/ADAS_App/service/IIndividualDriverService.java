package com.example.ADAS_App.service;

import com.example.ADAS_App.DTOs.CreateIndividualDriverDTO;
import com.example.ADAS_App.DTOs.IndividualDriverResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IIndividualDriverService {

    IndividualDriverResponseDTO createIndividualDriver(CreateIndividualDriverDTO dto);

    List<IndividualDriverResponseDTO> getAllIndividualDrivers();

    IndividualDriverResponseDTO getIndividualDriverById(UUID id);

    IndividualDriverResponseDTO updateIndividualDriver(UUID id, CreateIndividualDriverDTO dto);

    void deleteIndividualDriver(UUID id);
}
