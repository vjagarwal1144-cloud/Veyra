package com.vjagarwal.veyra;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/journeys")
public class JourneyController {
    private final SupabaseService supabase;

    public JourneyController(SupabaseService supabase) { this.supabase = supabase; }

    public record StartRequest(
            @NotBlank String destinationName,
            double latitude,
            double longitude,
            float wakeDistanceMeters,
            String transport,
            String protection,
            long startedAt,
            long endedAt,
            boolean alarmTriggered
    ) {}

    @PostMapping("/start")
    public Object start(@RequestBody StartRequest request) {
        return Map.of(
                "accepted", true,
                "localAlarmRequired", true,
                "destination", request.destinationName(),
                "cloudOptional", true
        );
    }

    @PostMapping("/sync")
    public ResponseEntity<?> sync(@RequestBody Map<String, Object> journey) {
        return ResponseEntity.ok(supabase.insertJourney(journey));
    }
}
