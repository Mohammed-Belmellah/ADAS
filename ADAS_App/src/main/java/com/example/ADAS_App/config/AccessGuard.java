package com.example.ADAS_App.config;
import com.example.ADAS_App.repository.CompanyDriverRepository;
import com.example.ADAS_App.repository.IndividualDriverRepository;
import com.example.ADAS_App.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccessGuard {
    private final UserDirectory dir;

    public AccessGuard(UserDirectory dir) {
        this.dir = dir;
    }

    public boolean isSelf(UUID driverId, Jwt jwt) {
        String claim = Optional.ofNullable(jwt.getClaimAsString("driverId"))
                .orElse(jwt.getClaimAsString("userId"));
        return claim != null && claim.equalsIgnoreCase(driverId.toString());
    }

    public boolean adminCanAccess(UUID driverId) {
        return dir.hasCompany(driverId);
    }

    public boolean canAccessSession(UUID sessionId, Jwt jwt) {
        UUID owner = dir.getDriverIdOfSession(sessionId);
        if (owner == null) return false;
        return isSelf(owner, jwt) || adminCanAccess(owner);
    }
}