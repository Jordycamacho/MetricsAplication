package com.fitapp.backend.infrastructure.security.auth.handler;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fitapp.backend.application.ports.output.UserPersistencePort;
import com.fitapp.backend.domain.model.FreeSubscriptionModel;
import com.fitapp.backend.domain.model.UserModel;
import com.fitapp.backend.infrastructure.persistence.entity.enums.Role;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SubscriptionType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

        private final JwtEncoder jwtEncoder;
        private final UserPersistencePort userRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.oauth2.redirect-uri:myapp://auth/callback}")
        private String redirectUri;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication) throws IOException {

                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

                String email = oauthUser.getAttribute("email");
                String googleId = oauthUser.getAttribute("sub");
                String name = oauthUser.getAttribute("name");
                String picture = oauthUser.getAttribute("picture");

                UserModel user = userRepository.findByGoogleId(googleId)
                                .orElseGet(() -> userRepository.findByEmail(email)
                                                .map(existing -> linkGoogleToExistingUser(existing, googleId, picture))
                                                .orElseGet(() -> createAndSaveOAuthUser(email, name, picture,
                                                                googleId)));

                String token = generateAppToken(user);

                response.sendRedirect("http://192.168.1.14:8080/api/auth/oauth2/success?token=" + token);

        }

        private UserModel linkGoogleToExistingUser(UserModel user, String googleId, String picture) {
                user.setGoogleId(googleId);
                user.setProvider("GOOGLE");
                if (user.getProfileImage() == null) {
                        user.setProfileImage(picture);
                }
                return userRepository.save(user);
        }

        private UserModel createAndSaveOAuthUser(String email, String name,
                        String picture, String googleId) {
                UserModel newUser = UserModel.builder()
                                .email(email)
                                .fullName(name)
                                .profileImage(picture)
                                .googleId(googleId)
                                .provider("GOOGLE")
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role(Role.USER)
                                .isActive(true)
                                .emailVerified(true)
                                .maxRoutines(1)
                                .subscription(FreeSubscriptionModel.builder()
                                                .startDate(LocalDate.now())
                                                .endDate(LocalDate.now().plusYears(1))
                                                .maxRoutines(1)
                                                .build())
                                .build();

                return userRepository.save(newUser);
        }

        private String generateAppToken(UserModel user) {
                Instant now = Instant.now();

                SubscriptionType subType = user.getSubscription() != null
                                ? user.getSubscription().getType()
                                : SubscriptionType.FREE;

                JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                                .issuer("AppFit")
                                .issuedAt(now)
                                .expiresAt(now.plus(12, ChronoUnit.HOURS))
                                .subject(user.getEmail())
                                .claim("userId", user.getId())
                                .claim("email", user.getEmail())
                                .claim("subscription", subType.name())
                                .claim("maxRoutines", user.getMaxRoutines())
                                .claim("roles", user.getGrantedAuthorities().stream()
                                                .map(GrantedAuthority::getAuthority)
                                                .collect(Collectors.toList()))
                                .build();

                return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
        }
}