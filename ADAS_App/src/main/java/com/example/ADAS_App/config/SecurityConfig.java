package com.example.ADAS_App.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity // allows @PreAuthorize on controller methods
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register/**").permitAll()
                        .requestMatchers("/public/**", "/ws/**","/ws-raw/**", "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /** Map Keycloak realm roles to Spring authorities (ROLE_*) */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractAuthorities);
        return converter;
    }

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return List.of();

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> roles)) return List.of();

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /** CORS for the React dev server (adjust origins for prod) */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173" , "http://localhost:5174"));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type"));
        c.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
