package com.example.ADAS_App.config;

import java.util.Optional;
import java.util.UUID;

public interface UserDirectory {
    /** true if the driver is a *company* driver (has a companyId); false if individual */
    boolean hasCompany(UUID driverId);

    /** returns the driverId who owns this session (or null if not found) */
    UUID getDriverIdOfSession(UUID sessionId);

    /** optional helper if you need it elsewhere */
    Optional<String> getUserRole(UUID userId); // e.g. ADMIN / DRIVER_COMPANY / DRIVER_INDEPENDENT
}

