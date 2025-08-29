package com.example.ADAS_App.config;

import com.example.ADAS_App.repository.SessionRepository;
import com.example.ADAS_App.repository.CompanyDriverRepository;
import com.example.ADAS_App.repository.IndividualDriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JpaUserDirectory implements UserDirectory {

    private final CompanyDriverRepository companyRepo;
    private final IndividualDriverRepository individualRepo;
    private final SessionRepository sessionRepo;

    @Override
    public boolean hasCompany(UUID driverId) {
        // If present in company drivers table, treat as company driver
        return companyRepo.findById(driverId)
                .map(cd -> cd.getId() != null)
                .orElse(false);
    }

    @Override
    public UUID getDriverIdOfSession(UUID sessionId) {
        return sessionRepo.findById(sessionId)
                .map(s -> s.getDriver().getId()) // adjust getter to your entity
                .orElse(null);
    }

    @Override
    public Optional<String> getUserRole(UUID userId) {
        if (companyRepo.existsById(userId)) return Optional.of("DRIVER_COMPANY");
        if (individualRepo.existsById(userId)) return Optional.of("DRIVER_INDEPENDENT");
        return Optional.empty();
    }
}