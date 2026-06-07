package com.fitapp.backend.auth.aplication.service;

import com.fitapp.backend.auth.aplication.dto.request.LoginRequest;
import com.fitapp.backend.auth.aplication.dto.request.RegisterRequest;
import com.fitapp.backend.auth.aplication.dto.request.ResetPasswordRequest;
import com.fitapp.backend.auth.aplication.dto.request.UserCreationRequest;
import com.fitapp.backend.auth.aplication.dto.response.AuthResponse;
import com.fitapp.backend.auth.aplication.dto.response.RegisterResponse;
import com.fitapp.backend.auth.aplication.port.input.AuthUseCase;
import com.fitapp.backend.auth.aplication.port.input.UserUseCase;
import com.fitapp.backend.auth.aplication.port.output.TokenBlacklistPort;
import com.fitapp.backend.auth.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.auth.domain.exception.EmailNotVerifiedException;
import com.fitapp.backend.auth.domain.model.CustomUserDetails;
import com.fitapp.backend.auth.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.infrastructure.shared.exception.EmailAlreadyExistsException;
import com.fitapp.backend.notification.aplication.port.input.NotificationUseCase;
import com.fitapp.backend.notification.infrastructure.ratelimit.EmailRateLimitService;

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
    private final NotificationUseCase notificationUseCase;
    private final EmailRateLimitService emailRateLimitService;

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

        user.verifyEmail();
        userPersistence.save(user);
        notificationUseCase.sendWelcomeEmail(user.getEmail(), user.getFullName());
        log.info("Email verificado para userId: {}", user.getId());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(Long userId) {
        UserModel user = userUseCase.findById(userId);

        if (user.isEmailVerified()) {
            throw new IllegalStateException("El correo ya está verificado");
        }

        emailRateLimitService.checkAndIncrement("resend", user.getEmail());
        sendVerificationEmail(user);
        log.info("Correo de verificación reenviado para userId: {}", userId);
    }

    @Override
    @Transactional
    public void resendVerificationByEmail(String email) {
        userUseCase.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                emailRateLimitService.checkAndIncrement("resend", email);
                sendVerificationEmail(user);
                log.info("Correo de verificación reenviado para email: {}", email);
            }
        });
    }

    // ── Registro ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
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

        log.info("Usuario registrado, userId: {} — pendiente verificación", userModel.getId());
        return RegisterResponse.builder()
                .email(userModel.getEmail())
                .message("Revisa tu correo para verificar tu cuenta antes de iniciar sesión")
                .requiresEmailVerification(true)
                .build();
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

        if (!userModel.isEmailVerified()) {
            log.warn("[AUTH] FALLO login: email no verificado={}", request.email());
            throw new EmailNotVerifiedException();
        }

        userUseCase.updateLastLogin(userModel.getId());
        log.info("[AUTH] Login exitoso userId={} roles={}",
                userModel.getId(), userModel.getGrantedAuthorities());
        return generateAuthResponse(userModel);
    }

    // ── Password reset ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(String email) {
        emailRateLimitService.checkAndIncrement("forgot", email);
        userUseCase.findByEmail(email).ifPresent(user -> {
            if ("GOOGLE".equalsIgnoreCase(user.getProvider())) {
                return;
            }
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userPersistence.save(user);
            notificationUseCase.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
            log.info("Correo de restablecimiento enviado para email: {}", email);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UserModel user = userPersistence.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new BadCredentialsException("Token de restablecimiento inválido o ya usado"));

        if (user.getPasswordResetTokenExpiresAt() != null
                && user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("El token de restablecimiento ha expirado. Solicita uno nuevo.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.clearPasswordResetToken();
        userPersistence.save(user);
        log.info("Contraseña restablecida para userId: {}", user.getId());
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

            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException();
            }

            log.info("[AUTH] Refresh exitoso para userId={} email={}", user.getId(), user.getEmail());
            return generateAuthResponse(user);
        } catch (BadCredentialsException | EmailNotVerifiedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AUTH] EXCEPCIÓN en refreshAccessToken: tipo={} mensaje={}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new BadCredentialsException("Token de refresco inválido o expirado");
        }
    }

    @Override
    public AuthResponse refreshToken(CustomUserDetails userDetails) {
        UserModel user = userUseCase.findById(userDetails.getUserId());
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }
        return generateAuthResponse(user);
    }

    // ── Helpers privados ────────────────────────────────────────────────────────

    private AuthResponse generateAuthResponse(UserModel userModel) {
        return new AuthResponse(
                jwtService.generateToken(userModel),
                jwtService.generateRefreshToken(userModel),
                Instant.now().plus(12, ChronoUnit.HOURS));
    }

    private void sendVerificationEmail(UserModel user) {
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));
        userPersistence.save(user);
        notificationUseCase.sendVerificationEmail(user.getEmail(), token);
    }
}
