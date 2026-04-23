package com.fitapp.backend.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.fitapp.backend.application.ports.input.JwtService;
import com.fitapp.backend.user.domain.model.UserModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtEncoder jwtEncoder;
    private final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);

    public String generateToken(UserModel user) {
        log.info("[JWT] Generando ACCESS token para userId={} email={}", user.getId(), user.getEmail());
        String token = buildToken(user, 12 * 60, "access");
        log.info("[JWT] ACCESS token generado, expira en 12 horas. userId={}", user.getId());
        return token;
    }

    public String generateRefreshToken(UserModel user) {
        log.info("[JWT] Generando REFRESH token para userId={} email={}", user.getId(), user.getEmail());
        String token = buildToken(user, 7 * 24 * 60, "refresh");
        log.info("[JWT] REFRESH token generado, expira en 7 días. userId={}", user.getId());
        return token;
    }

    private String buildToken(UserModel user, long expirationMinutes, String type) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        log.debug("[JWT] Construyendo token type={} userId={} issuedAt={} expiresAt={}",
                type, user.getId(), now, expiresAt);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", getRoles(user));
        claims.put("type", type);

        log.debug("[JWT] Claims incluidos: userId={}, email={}, roles={}, type={}",
                user.getId(), user.getEmail(), getRoles(user), type);

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("AppFit")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claims(c -> c.putAll(claims))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    private List<String> getRoles(UserModel user) {
        return user.getGrantedAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}