package com.fitapp.backend.infrastructure.security.auth.converter;

import java.util.Collection;
import java.util.Collections;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import com.fitapp.backend.infrastructure.security.auth.model.CustomUserDetails;
import com.fitapp.backend.user.aplication.port.output.UserPersistencePort;
import com.fitapp.backend.user.domain.model.UserModel;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserPersistencePort userRepository;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthConverter.class);
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public JwtAuthConverter(UserPersistencePort userRepository) {
        this.userRepository = userRepository;
        this.jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        this.jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        String type = jwt.getClaimAsString("type");

        log.info("[JWT_CONVERTER] Convirtiendo token: subject={} type={} expiresAt={}",
                jwt.getSubject(), type, jwt.getExpiresAt());

        if ("refresh".equals(type)) {
            log.warn("[JWT_CONVERTER] ⚠️  Se recibió un REFRESH token como Bearer de autenticación. " +
                    "El frontend debe usar el access token, no el refresh token.");
        }

        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[JWT_CONVERTER] Usuario no encontrado para email={}", email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        log.debug("[JWT_CONVERTER] Usuario encontrado: userId={} roles={}",
                user.getId(), user.getGrantedAuthorities());

        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        if (authorities == null) {
            authorities = Collections.emptyList();
        }

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
        authentication.setDetails(new CustomUserDetails(user));

        log.info("[JWT_CONVERTER] Token convertido OK para userId={}", user.getId());
        return authentication;
    }
}