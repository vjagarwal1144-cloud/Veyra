package com.vjagarwal.veyra;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
public class PushController {
    private final FirebasePushService pushService;
    public PushController(FirebasePushService pushService) { this.pushService = pushService; }

    public record PushRequest(
            @NotBlank String token,
            @NotBlank String title,
            @NotBlank String body
    ) {}

    @PostMapping("/send")
    public Object send(@RequestBody PushRequest request) {
        return pushService.send(request.token(), request.title(), request.body());
    }
}
