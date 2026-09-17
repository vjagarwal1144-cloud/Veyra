package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/maps")
public class MapsController {
    private final String key;
    private final String placesUrl;
    private final String routesUrl;
    private final String geocodeUrl;
    private final RestClient client = RestClient.builder().build();

    public MapsController(
            @Value("${veyra.google.api-key:}") String key,
            @Value("${veyra.google.places-url:https://places.googleapis.com/v1}") String placesUrl,
            @Value("${veyra.google.routes-url:https://routes.googleapis.com/directions/v2:computeRoutes}") String routesUrl,
            @Value("${veyra.google.geocode-url:https://maps.googleapis.com/maps/api/geocode/json}") String geocodeUrl
    ) {
        this.key = key;
        this.placesUrl = placesUrl;
        this.routesUrl = routesUrl;
        this.geocodeUrl = geocodeUrl;
    }

    @GetMapping("/geocode")
    public Object geocode(@RequestParam String address) {
        if (!enabled()) return disabled();
        return client.get()
                .uri(uriBuilder -> uriBuilder.fromUriString(geocodeUrl).queryParam("address", address).queryParam("key", key).build())
                .retrieve()
                .body(Object.class);
    }

    @PostMapping("/search")
    public Object search(@RequestBody Map<String, Object> request) {
        if (!enabled()) return disabled();
        String query = String.valueOf(request.getOrDefault("query", "")).trim();
        if (query.isEmpty()) return Map.of("enabled", true, "error", "query is required");
        Map<String, Object> body = new HashMap<>();
        body.put("textQuery", query);
        body.put("languageCode", request.getOrDefault("languageCode", "en"));
        body.put("regionCode", request.getOrDefault("regionCode", "IN"));
        return client.post()
                .uri(placesUrl + "/places:searchText")
                .header("X-Goog-Api-Key", key)
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location,places.googleMapsUri")
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    @PostMapping("/route")
    public Object route(@RequestBody Map<String, Object> request) {
        if (!enabled()) return disabled();
        Map<String, Object> body = new HashMap<>();
        body.put("origin", point(request, "originLat", "originLon"));
        body.put("destination", point(request, "destinationLat", "destinationLon"));
        body.put("travelMode", String.valueOf(request.getOrDefault("travelMode", "DRIVE")).toUpperCase());
        body.put("computeAlternativeRoutes", false);
        body.put("units", "METRIC");
        return client.post()
                .uri(routesUrl)
                .header("X-Goog-Api-Key", key)
                .header("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.legs.steps.navigationInstruction")
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    private Map<String, Object> point(Map<String, Object> request, String latKey, String lonKey) {
        Map<String, Object> latLng = Map.of(
                "latitude", Double.parseDouble(String.valueOf(request.get(latKey))),
                "longitude", Double.parseDouble(String.valueOf(request.get(lonKey)))
        );
        return Map.of("location", Map.of("latLng", latLng));
    }

    private boolean enabled() { return key != null && !key.isBlank(); }
    private Map<String, Object> disabled() { return Map.of("enabled", false, "message", "Google provider is not configured"); }
}
