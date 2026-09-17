package com.vjagarwal.veyra;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {
    private final SupabaseService supabase;

    public AuthController(SupabaseService supabase) { this.supabase = supabase; }

    public record Credentials(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 128) String password
    ) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials request) {
        return ResponseEntity.ok(supabase.login(request.email(), request.password()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Credentials request) {
        return ResponseEntity.ok(supabase.signup(request.email(), request.password()));
    }
}
