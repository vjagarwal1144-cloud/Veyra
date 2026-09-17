package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain security(
            HttpSecurity http,
            @Value("${veyra.security.jwt-enabled:false}") boolean jwtEnabled
    ) throws Exception {
        http.csrf(csrf -> csrf.disable());

        if (jwtEnabled) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/health", "/actuator/health", "/api/public/**").permitAll()
                    .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> { }));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }
        return http.build();
    }
}
