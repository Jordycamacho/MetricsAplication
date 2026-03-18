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
    private final UserPersistencePort userPersistence;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final EmailService emailService;

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    // ── Logout ──────────────────────────────────────────────────────────────────

    @Override
    public void logout(String bearerToken) {
        log.info("[AUTH] Solicitud de logout recibida");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.warn("[AUTH] FALLO logout: header inválido='{}'", bearerToken);
            throw new BadCredentialsException("Token inválido");
        }

        String token = bearerToken.substring(7);
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Instant expiry = jwt.getExpiresAt();
            long ttl = expiry != null ? Instant.now().until(expiry, ChronoUnit.SECONDS) : 0;
            log.info("[AUTH] Token a blacklistear: subject={} expiresAt={} ttlRestante={}s",
                    jwt.getSubject(), expiry, ttl);
            tokenBlacklistPort.blacklist(token, ttl);
            log.info("[AUTH] Logout exitoso - token invalidado en Redis");
        } catch (Exception e) {
            log.warn("[AUTH] Logout con token ya inválido o expirado: {}", e.getMessage());
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
        log.info("[AUTH] Intento de login para email: {}", request.email());

        UserModel userModel = userUseCase.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("[AUTH] FALLO login: email no encontrado={}", request.email());
                    return new BadCredentialsException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(request.password(), userModel.getPassword())) {
            log.warn("[AUTH] FALLO login: contraseña incorrecta para email={}", request.email());
            throw new BadCredentialsException("Credenciales inválidas");
        }

        userUseCase.updateLastLogin(userModel.getId());
        log.info("[AUTH] Login exitoso userId={} roles={}",
                userModel.getId(), userModel.getGrantedAuthorities());
        return generateAuthResponse(userModel);
    }

    // ── Refresh ─────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        log.info("[AUTH] Intento de refresh con token: {}...",
                refreshToken.length() > 20 ? refreshToken.substring(0, 20) : refreshToken);
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            log.info("[AUTH] Refresh token decodificado OK. subject={} expiresAt={}",
                    jwt.getSubject(), jwt.getExpiresAt());

            String type = jwt.getClaimAsString("type");
            log.info("[AUTH] Tipo de token recibido en /refresh: '{}'", type);

            if (!"refresh".equals(type)) {
                log.warn("[AUTH] FALLO refresh: el token tiene type='{}', se esperaba 'refresh'", type);
                throw new BadCredentialsException("Token proporcionado no es un refresh token");
            }

            UserModel user = userUseCase.findByEmail(jwt.getSubject())
                    .orElseThrow(() -> {
                        log.warn("[AUTH] FALLO refresh: usuario no encontrado para email={}", jwt.getSubject());
                        return new BadCredentialsException("Token inválido");
                    });

            log.info("[AUTH] Refresh exitoso para userId={} email={}", user.getId(), user.getEmail());
            return generateAuthResponse(user);
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AUTH] EXCEPCIÓN en refreshAccessToken: tipo={} mensaje={}",
                    e.getClass().getSimpleName(), e.getMessage());
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