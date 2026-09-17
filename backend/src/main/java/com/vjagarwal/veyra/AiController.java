package com.vjagarwal.veyra;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;
    public AiController(AiService aiService) { this.aiService = aiService; }

    public record Prompt(@NotBlank String prompt) {}

    @PostMapping("/explain")
    public Object explain(@RequestBody Prompt request) {
        return aiService.explain(request.prompt());
    }
}
