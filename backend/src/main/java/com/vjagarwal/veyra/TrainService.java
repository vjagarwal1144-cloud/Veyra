package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class TrainService {
    private final String baseUrl;
    private final String apiKey;
    private final String searchPath;
    private final RestClient client = RestClient.builder().build();

    public TrainService(
            @Value("${veyra.train.base-url:}") String baseUrl,
            @Value("${veyra.train.api-key:}") String apiKey,
            @Value("${veyra.train.search-path:/trains/{number}}") String searchPath
    ) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.searchPath = searchPath;
    }

    public Object find(String number) {
        if (baseUrl == null || baseUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            return Map.of("enabled", false, "message", "Train provider is not configured");
        }
        String path = searchPath.replace("{number}", number);
        return client.get().uri(baseUrl + path)
                .header("Authorization", "Bearer " + apiKey)
                .retrieve().body(Object.class);
    }
}
