package com.example.ADAS_App.Mappers;


import com.example.ADAS_App.DTOs.CompanyDTO;
import com.example.ADAS_App.entity.Company;

public class CompanyMapper {

    public static CompanyDTO toDTO(Company company) {
        if (company == null) return null;
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setIndustry(company.getIndustry());
        dto.setAddress(company.getAddress());
        return dto;
    }
    public static Company toEntity(CompanyDTO dto) {
        if (dto == null) return null;
        Company company = new Company();
        company.setId(dto.getId()); // allow updates if ID is provided
        company.setName(dto.getName());
        company.setIndustry(dto.getIndustry());
        company.setAddress(dto.getAddress());
        return company;
    }
}