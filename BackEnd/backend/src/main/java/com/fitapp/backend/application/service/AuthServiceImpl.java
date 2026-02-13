package com.fitapp.backend.application.service;


import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.domain.exception.EmailAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.dto.Auth.RegisterRequest;
import com.fitapp.backend.application.ports.input.AuthUseCase;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.application.ports.input.JwtService;
import com.fitapp.backend.application.dto.Auth.AuthResponse;
import com.fitapp.backend.application.dto.Auth.LoginRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import com.fitapp.backend.domain.model.UserModel;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.temporal.ChronoUnit;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import org.slf4j.Logger;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final UserUseCase userUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtDecoder jwtDecoder;
    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        log.info("Intentando refrescar token");
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String email = jwt.getSubject();

            UserModel user = userUseCase.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Refresh token con email no existente: {}", email);
                        return new BadCredentialsException("Token inválido");
                    });

            AuthResponse response = generateAuthResponse(user);
            log.info("Token refrescado exitosamente para userId: {}", user.getId());
            return response;
        } catch (Exception e) {
            log.error("Fallo al refrescar token: {}", e.getMessage());
            throw new BadCredentialsException("Token de refresco inválido o expirado");
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Intento de registro con email: {}", request.getEmail());
        
        if (userUseCase.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registro fallido: email ya existe - {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        log.info("Usuario registrado exitosamente");
        UserCreationRequest userCreationRequest = UserCreationRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .role(Role.USER)
                .isActive(true)
                .maxRoutines(1)
                .build();

        UserModel userModel = userUseCase.createUser(userCreationRequest);

        return generateAuthResponse(userModel);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para email: {}", request.email());
        UserModel userModel = userUseCase.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (userModel.getSubscription() != null) {
            userModel.getSubscription().getType();
        }

        if (!passwordEncoder.matches(request.password(), userModel.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        
        log.info("Login exitoso: userId={}, email={}", userModel.getId(), userModel.getEmail());
        userUseCase.updateLastLogin(userModel.getId());
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
                Instant.now().plus(12, ChronoUnit.HOURS));
    }
}