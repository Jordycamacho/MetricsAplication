package com.fitapp.backend.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitapp.backend.application.dto.Auth.AuthResponse;
import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.adapter.out.supabase.SupabaseAuthClient;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserUseCase userService;
    private final SupabaseAuthClient supabaseClient;
    private final int DEFAULT_MAX_ROUTINES = 1;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        AuthResponse auth = supabaseClient.signIn(request.email(), request.password());

        UserModel user = userService.findBySupabaseUid(auth.getUserId())
                .orElseGet(() -> userService.createUser(
                        new UserCreationRequest(
                                auth.getUserId(),
                                auth.getEmail(),
                                Role.STANDARD,
                                true,
                                DEFAULT_MAX_ROUTINES)));

        userService.updateLastLogin(user.getId());

        return ResponseEntity.ok(new LoginResponse(auth.getAccessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            supabaseClient.signOut(token);
        }
        return ResponseEntity.ok().build();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String accessToken) {
    }
}
