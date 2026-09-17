package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/maps")
public class MapsController {
    private final String base; private final String key; private final RestClient client = RestClient.builder().build();
    public MapsController(@Value("${veyra.maps-base-url}") String base, @Value("${veyra.maps-key}") String key) { this.base = base; this.key = key; }
    @GetMapping("/geocode") public Object geocode(@RequestParam String address) {
        if (key == null || key.isBlank()) return java.util.Map.of("enabled", false, "message", "Maps provider key is not configured");
        return client.get().uri(base + "/geocode/json?address={address}&key={key}", address, key).retrieve().body(Object.class);
    }
}
