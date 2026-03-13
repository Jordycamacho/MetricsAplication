package com.fitapp.backend.infrastructure.security.auth.handler;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fitapp.backend.application.ports.input.SubscriptionUseCase;
import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.SubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

        private final JwtEncoder jwtEncoder;
        private final UserPersistencePort userRepository;
        private final PasswordEncoder passwordEncoder;
        private final SubscriptionUseCase subscriptionUseCase;

        public OAuth2SuccessHandler(JwtEncoder jwtEncoder,
                        UserPersistencePort userRepository,
                        PasswordEncoder passwordEncoder,
                        @Lazy SubscriptionUseCase subscriptionUseCase) {
                this.jwtEncoder = jwtEncoder;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.subscriptionUseCase = subscriptionUseCase;
        }

        @Value("${app.oauth2.redirect-uri:fitapp://auth/callback}")
        private String redirectUri;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication) throws IOException {

                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                String email = oauthUser.getAttribute("email");
                String googleId = oauthUser.getAttribute("sub");
                String name = oauthUser.getAttribute("name");

                UserModel user = resolveUser(email, googleId, name);

                String accessToken = generateAppToken(user);
                String refreshToken = generateRefreshRefToken(user);
                String expiresAt = Instant.now().plus(12, ChronoUnit.HOURS).toString();

                if (user.getSubscription() == null) {
                        SubscriptionModel sub = subscriptionUseCase.createFreeSubscription(user.getId());
                        user.setSubscription(sub);
                }

                response.sendRedirect(
                                redirectUri
                                                + "?token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                                                + "&refreshToken="
                                                + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                                                + "&expiresAt=" + URLEncoder.encode(expiresAt, StandardCharsets.UTF_8));
        }

        // ── Resolución de usuario ────────────────────────────────────────────────

        private UserModel resolveUser(String email, String googleId, String name) {
                Optional<UserModel> byGoogle = userRepository.findByGoogleId(googleId);
                if (byGoogle.isPresent()) {
                        return byGoogle.get();
                }

                Optional<UserModel> byEmail = userRepository.findByEmail(email);
                if (byEmail.isPresent()) {
                        return linkGoogleToExistingUser(byEmail.get(), googleId);
                }

                return createOAuthUser(email, name, googleId);
        }

        private UserModel linkGoogleToExistingUser(UserModel user, String googleId) {
                user.setGoogleId(googleId);
                user.setProvider("GOOGLE");
                return userRepository.save(user);
        }

        private UserModel createOAuthUser(String email, String name, String googleId) {
                UserModel newUser = UserModel.builder()
                                .email(email)
                                .fullName(name)
                                .googleId(googleId)
                                .provider("GOOGLE")
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role(Role.USER)
                                .isActive(true)
                                .emailVerified(true)
                                .build();
                return userRepository.save(newUser);
        }

        // ── JWT ──────────────────────────────────────────────────────────────────

        private String generateRefreshRefToken(UserModel user) {
                Instant now = Instant.now();
                JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer("AppFit")
                                .issuedAt(now)
                                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                                .subject(user.getEmail())
                                .claim("userId", user.getId())
                                .claim("email", user.getEmail())
                                .claim("type", "refresh")
                                .claim("roles", user.getGrantedAuthorities().stream()
                                                .map(GrantedAuthority::getAuthority)
                                                .collect(Collectors.toList()))
                                .build();
                return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        }

        private String generateAppToken(UserModel user) {
                Instant now = Instant.now();

                SubscriptionType subType = user.getSubscription() != null
                                ? user.getSubscription().getType()
                                : SubscriptionType.FREE;

                JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer("AppFit")
                                .issuedAt(now)
                                .expiresAt(now.plus(12, ChronoUnit.HOURS))
                                .subject(user.getEmail())
                                .claim("userId", user.getId())
                                .claim("email", user.getEmail())
                                .claim("subscription", subType.name())
                                .claim("roles", user.getGrantedAuthorities().stream()
                                                .map(GrantedAuthority::getAuthority)
                                                .collect(Collectors.toList()))
                                .build();

                return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        }
}