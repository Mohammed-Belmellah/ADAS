package com.example.ADAS_App.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// src/main/java/.../config/KeycloakAdminConfig.java
@Configuration
@ConfigurationProperties(prefix = "keycloak.admin")
@Data
class KeycloakAdminProps {
    private String serverUrl;   // http://localhost:8081
    private String realm;       // adas
    private String clientId;    // adas-admin
    private String clientSecret;
}

@Configuration
@RequiredArgsConstructor
public class KeycloakAdminConfig {
    private final KeycloakAdminProps p;

    @Bean
    public org.keycloak.admin.client.Keycloak keycloakAdmin() {
        return org.keycloak.admin.client.KeycloakBuilder.builder()
                .serverUrl(p.getServerUrl())
                .realm(p.getRealm())
                .clientId(p.getClientId())
                .clientSecret(p.getClientSecret())
                .grantType(org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
