package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private final String provider;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final RestClient client = RestClient.builder().build();

    public AiService(
            @Value("${veyra.ai.provider:none}") String provider,
            @Value("${veyra.ai.api-key:}") String apiKey,
            @Value("${veyra.ai.model:}") String model,
            @Value("${veyra.ai.base-url:}") String baseUrl
    ) {
        this.provider = provider == null ? "none" : provider.toLowerCase();
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    public Object explain(String prompt) {
        if (apiKey == null || apiKey.isBlank() || "none".equals(provider)) {
            return Map.of("enabled", false, "message", "AI provider is not configured");
        }
        return switch (provider) {
            case "openai" -> openAi(prompt);
            case "gemini" -> gemini(prompt);
            case "anthropic", "claude" -> anthropic(prompt);
            default -> Map.of("enabled", false, "message", "Unsupported AI provider: " + provider);
        };
    }

    private Object openAi(String prompt) {
        String url = (baseUrl == null || baseUrl.isBlank()) ? "https://api.openai.com/v1/chat/completions" : baseUrl;
        Map<String, Object> body = Map.of(
                "model", model == null || model.isBlank() ? "gpt-4.1-mini" : model,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        return client.post().uri(url)
                .header("Authorization", "Bearer " + apiKey)
                .body(body).retrieve().body(Object.class);
    }

    private Object gemini(String prompt) {
        String selected = model == null || model.isBlank() ? "gemini-2.5-flash" : model;
        String base = (baseUrl == null || baseUrl.isBlank()) ? "https://generativelanguage.googleapis.com/v1beta" : baseUrl;
        Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        return client.post().uri(base + "/models/" + selected + ":generateContent?key=" + apiKey)
                .body(body).retrieve().body(Object.class);
    }

    private Object anthropic(String prompt) {
        String url = (baseUrl == null || baseUrl.isBlank()) ? "https://api.anthropic.com/v1/messages" : baseUrl;
        Map<String, Object> body = Map.of(
                "model", model == null || model.isBlank() ? "claude-3-5-haiku-latest" : model,
                "max_tokens", 512,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        return client.post().uri(url)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .body(body).retrieve().body(Object.class);
    }
}
