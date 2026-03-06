package com.fitapp.backend.application.service;

import com.fitapp.backend.application.dto.Auth.AuthResponse;
import com.fitapp.backend.application.dto.Auth.LoginRequest;
import com.fitapp.backend.application.dto.Auth.RegisterRequest;
import com.fitapp.backend.application.dto.user.UserCreationRequest;
import com.fitapp.backend.application.ports.input.AuthUseCase;
import com.fitapp.backend.application.ports.input.JwtService;
import com.fitapp.backend.application.ports.input.UserUseCase;
import com.fitapp.backend.application.ports.output.TokenBlacklistPort;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.exception.EmailAlreadyExistsException;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final UserUseCase userUseCase;
    private final UserPersistencePort userPersistence; // acceso directo para persistir tokens de verificación
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final EmailService emailService;

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    // ── Logout ──────────────────────────────────────────────────────────────────

    @Override
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token inválido");
        }

        String token = bearerToken.substring(7);

        try {
            Jwt jwt = jwtDecoder.decode(token);
            Instant expiry = jwt.getExpiresAt();
            long ttl = expiry != null ? Instant.now().until(expiry, ChronoUnit.SECONDS) : 0;
            tokenBlacklistPort.blacklist(token, ttl);
            log.info("Logout exitoso - token invalidado");
        } catch (Exception e) {
            // Token ya expirado o malformado: logout igualmente seguro
            log.warn("Logout con token ya inválido: {}", e.getMessage());
        }
    }

    // ── Verificación de correo ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void verifyEmail(String token) {
        UserModel user = userPersistence.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadCredentialsException("Token de verificación inválido o ya usado"));

        if (user.getEmailVerificationTokenExpiresAt() != null
                && user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("El token de verificación ha expirado. Solicita uno nuevo.");
        }

        user.verifyEmail(); // limpia token y marca emailVerified = true
        userPersistence.save(user);
        log.info("Email verificado para userId: {}", user.getId());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(Long userId) {
        UserModel user = userUseCase.findById(userId);

        if (user.isEmailVerified()) {
            throw new IllegalStateException("El correo ya está verificado");
        }

        sendVerificationEmail(user);
        log.info("Correo de verificación reenviado para userId: {}", userId);
    }

    // ── Registro ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Intento de registro con email: {}", request.getEmail());

        if (userUseCase.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        UserModel userModel = userUseCase.createUser(UserCreationRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .role(Role.USER)
                .isActive(true)
                .build());

        sendVerificationEmail(userModel);

        log.info("Usuario registrado, userId: {}", userModel.getId());
        return generateAuthResponse(userModel);
    }

    // ── Login ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para email: {}", request.email());

        UserModel userModel = userUseCase.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), userModel.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        userUseCase.updateLastLogin(userModel.getId());
        log.info("Login exitoso userId={}", userModel.getId());
        return generateAuthResponse(userModel);
    }

    // ── Refresh ─────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            UserModel user = userUseCase.findByEmail(jwt.getSubject())
                    .orElseThrow(() -> new BadCredentialsException("Token inválido"));
            return generateAuthResponse(user);
        } catch (Exception e) {
            log.error("Fallo al refrescar token: {}", e.getMessage());
            throw new BadCredentialsException("Token de refresco inválido o expirado");
        }
    }

    @Override
    public AuthResponse refreshToken(CustomUserDetails userDetails) {
        return generateAuthResponse(userUseCase.findById(userDetails.getUserId()));
    }

    // ── Helpers privados ────────────────────────────────────────────────────────

    private AuthResponse generateAuthResponse(UserModel userModel) {
        return new AuthResponse(
                jwtService.generateToken(userModel),
                jwtService.generateRefreshToken(userModel),
                Instant.now().plus(12, ChronoUnit.HOURS));
    }

    /** Genera token UUID, lo persiste en el usuario y envía el correo. */
    private void sendVerificationEmail(UserModel user) {
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));
        userPersistence.save(user);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }
}