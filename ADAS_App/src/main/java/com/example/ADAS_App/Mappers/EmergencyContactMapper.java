package com.example.ADAS_App.Mappers;


import com.example.ADAS_App.DTOs.EmergencyContactDTO;
import com.example.ADAS_App.entity.EmergencyContact;
import com.example.ADAS_App.entity.IndividualDriver;

public class EmergencyContactMapper {

    public static EmergencyContactDTO toDTO(EmergencyContact contact) {
        if (contact == null) return null;
        EmergencyContactDTO dto = new EmergencyContactDTO();
        dto.setId(contact.getId());
        dto.setDriverId(contact.getDriver().getId());
        dto.setName(contact.getName());
        dto.setPhone(contact.getPhone());
        dto.setRelation(contact.getRelation());
        return dto;
    }

    public static EmergencyContact toEntity(EmergencyContactDTO dto, IndividualDriver driver) {
        if (dto == null) return null;
        EmergencyContact contact = new EmergencyContact();
        contact.setId(dto.getId()); // keep ID if updating
        contact.setDriver(driver);  // must fetch driver first
        contact.setName(dto.getName());
        contact.setPhone(dto.getPhone());
        contact.setRelation(dto.getRelation());
        return contact;
    }
}
