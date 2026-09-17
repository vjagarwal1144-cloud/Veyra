package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class SupabaseService {
    private final String url;
    private final String anonKey;
    private final String serviceRoleKey;
    private final RestClient client = RestClient.builder().build();

    public SupabaseService(
            @Value("${veyra.supabase.url:}") String url,
            @Value("${veyra.supabase.anon-key:}") String anonKey,
            @Value("${veyra.supabase.service-role-key:}") String serviceRoleKey
    ) {
        this.url = url;
        this.anonKey = anonKey;
        this.serviceRoleKey = serviceRoleKey;
    }

    public boolean configured() { return url != null && !url.isBlank() && anonKey != null && !anonKey.isBlank(); }
    public boolean persistenceConfigured() { return configured() && serviceRoleKey != null && !serviceRoleKey.isBlank(); }

    public Object login(String email, String password) {
        if (!configured()) return disabled();
        return client.post()
                .uri(url + "/auth/v1/token?grant_type=password")
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + anonKey)
                .body(Map.of("email", email, "password", password))
                .retrieve()
                .body(Object.class);
    }

    public Object signup(String email, String password) {
        if (!configured()) return disabled();
        return client.post()
                .uri(url + "/auth/v1/signup")
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + anonKey)
                .body(Map.of("email", email, "password", password))
                .retrieve()
                .body(Object.class);
    }

    public Object insertJourney(Object journey) {
        if (!persistenceConfigured()) return disabledPersistence();
        return client.post()
                .uri(url + "/rest/v1/journeys")
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Prefer", "return=minimal")
                .body(journey)
                .retrieve()
                .body(Object.class);
    }

    private Map<String, Object> disabled() { return Map.of("enabled", false, "message", "Supabase is not configured"); }
    private Map<String, Object> disabledPersistence() { return Map.of("enabled", false, "message", "Supabase persistence service role is not configured"); }
}
