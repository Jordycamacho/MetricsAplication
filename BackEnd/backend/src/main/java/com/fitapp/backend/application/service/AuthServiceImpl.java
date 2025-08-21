package com.fitapp.backend.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fitapp.backend.application.dto.Auth.AuthResponse;
import com.fitapp.backend.application.dto.Auth.LoginRequest;
import com.fitapp.backend.application.dto.Auth.RegisterRequest;
import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.ports.input.AuthUseCase;
import com.fitapp.backend.application.ports.input.JwtService;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.domain.exception.EmailAlreadyExistsException;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final UserUseCase userUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verificar si el email ya existe
        if (userUseCase.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        UserCreationRequest userCreationRequest = UserCreationRequest.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .isActive(true)
                .maxRoutines(1)
                .build();

        UserModel userModel = userUseCase.createUser(userCreationRequest);
        
        return generateAuthResponse(userModel);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email
        UserModel userModel = userUseCase.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.password(), userModel.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Actualizar último login
        userUseCase.updateLastLogin(userModel.getId());

        // Generar tokens
        return generateAuthResponse(userModel);
    }

    @Override
    public AuthResponse refreshToken(CustomUserDetails userDetails) {

        UserModel userModel = userUseCase.findById(userDetails.getUserId());
        if (userModel == null) {
            throw new RuntimeException("User not found with id: " + userDetails.getUserId());
        }
        return generateAuthResponse(userModel);
    }

    private AuthResponse generateAuthResponse(UserModel userModel) {
        String accessToken = jwtService.generateToken(userModel);
        String refreshToken = jwtService.generateRefreshToken(userModel);
        
        return new AuthResponse(
            accessToken,
            refreshToken,
            Instant.now().plus(12, ChronoUnit.HOURS)
        );
    }
}