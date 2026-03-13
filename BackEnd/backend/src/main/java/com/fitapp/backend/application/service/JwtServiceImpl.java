package com.fitapp.backend.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.fitapp.backend.application.ports.input.JwtService;
import com.fitapp.backend.domain.model.UserModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtEncoder jwtEncoder;
    
    public String generateToken(UserModel user) {
        return buildToken(user, 12 * 60, "access");
    }
    
    public String generateRefreshToken(UserModel user) {
        return buildToken(user, 7 * 24 * 60, "refresh");
    }
    
    private String buildToken(UserModel user, long expirationMinutes, String type) {
        Instant now = Instant.now();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", getRoles(user));
        claims.put("type", type);
        
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .issuer("JnobFit")
            .issuedAt(now)
            .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
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
