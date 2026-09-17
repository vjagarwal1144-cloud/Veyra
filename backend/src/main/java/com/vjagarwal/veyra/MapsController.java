package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/maps")
public class MapsController {
    private final String geocodeUrl;
    private final String routeUrl;
    private final String userAgent;
    private final RestClient client;

    public MapsController(
            @Value("${veyra.osm.geocode-url:https://nominatim.openstreetmap.org/search}") String geocodeUrl,
            @Value("${veyra.osm.route-url:https://router.project-osrm.org/route/v1}") String routeUrl,
            @Value("${veyra.osm.user-agent:Veyra/1.0 (https://github.com/vjagarwal1144-cloud/Veyra)}") String userAgent
    ) {
        this.geocodeUrl = geocodeUrl;
        this.routeUrl = routeUrl;
        this.userAgent = userAgent;
        this.client = RestClient.builder().defaultHeader("User-Agent", userAgent).build();
    }

    @GetMapping("/geocode")
    public Object geocode(@RequestParam String address) {
        if (address == null || address.isBlank()) {
            return Map.of("enabled", true, "error", "address is required");
        }
        String uri = UriComponentsBuilder.fromUriString(geocodeUrl)
                .queryParam("q", address)
                .queryParam("format", "jsonv2")
                .queryParam("limit", 5)
                .queryParam("countrycodes", "in")
                .build()
                .encode()
                .toUriString();
        return client.get().uri(uri).retrieve().body(Object.class);
    }

    @PostMapping("/search")
    public Object search(@RequestBody Map<String, Object> request) {
        String query = String.valueOf(request.getOrDefault("query", "")).trim();
        if (query.isEmpty()) return Map.of("enabled", true, "error", "query is required");
        return geocode(query);
    }

    @PostMapping("/route")
    public Object route(@RequestBody Map<String, Object> request) {
        double originLat = number(request, "originLat");
        double originLon = number(request, "originLon");
        double destinationLat = number(request, "destinationLat");
        double destinationLon = number(request, "destinationLon");
        String travelMode = String.valueOf(request.getOrDefault("travelMode", "DRIVE")).toUpperCase();

        String profile = switch (travelMode) {
            case "WALK", "WALKING", "FOOT" -> "foot";
            case "BIKE", "BICYCLE", "CYCLING" -> "bike";
            default -> "driving";
        };

        String coordinates = originLon + "," + originLat + ";" + destinationLon + "," + destinationLat;
        String uri = routeUrl + "/" + profile + "/" + coordinates;
        uri = UriComponentsBuilder.fromUriString(uri)
                .queryParam("overview", "full")
                .queryParam("geometries", "geojson")
                .queryParam("steps", "true")
                .queryParam("alternatives", "false")
                .build()
                .encode()
                .toUriString();

        Object response = client.get().uri(uri).retrieve().body(Object.class);
        Map<String, Object> result = new HashMap<>();
        result.put("provider", "OSRM");
        result.put("travelMode", profile);
        result.put("data", response);
        return result;
    }

    private double number(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) throw new IllegalArgumentException(key + " is required");
        return Double.parseDouble(String.valueOf(value));
    }
}
