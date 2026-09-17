package com.vjagarwal.veyra;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journeys")
public class JourneyController {
    public record StartRequest(@NotBlank String destinationName, double latitude, double longitude, float wakeDistanceMeters, String transport) {}
    @PostMapping("/start") public Object start(@RequestBody StartRequest request) {
        return java.util.Map.of("accepted", true, "localAlarmRequired", true, "destination", request.destinationName());
    }
}
