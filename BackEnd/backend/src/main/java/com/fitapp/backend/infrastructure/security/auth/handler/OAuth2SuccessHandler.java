package com.fitapp.backend.infrastructure.security.auth.handler;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication) throws IOException {

                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                String email = oauthUser.getAttribute("email");

                UserModel user = userRepository.findByEmail(email)
                                .orElseGet(() -> createUserFromOAuth(oauthUser, email));

                String token = generateAppToken(user);
                String redirectUrl = String.format("http://localhost:3000/auth/callback?token=%s", token);
                response.sendRedirect(redirectUrl);
        }

        private String generateAppToken(UserModel user) {
                Instant now = Instant.now();

                Map<String, Object> claims = new HashMap<>();
                claims.put("userId", user.getId());

                SubscriptionType subType = user.getSubscription() != null ? user.getSubscription().getType()
                                : SubscriptionType.FREE;

                claims.put("subscription", subType.name());
                claims.put("maxRoutines", user.getMaxRoutines());

                List<String> roles = user.getGrantedAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());

                claims.put("roles", roles);

                JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                                .issuer("AppFit")
                                .issuedAt(now)
                                .expiresAt(now.plus(12, ChronoUnit.HOURS))
                                .subject(user.getEmail())
                                .claim("email", user.getEmail())
                                .claims(c -> c.putAll(claims))
                                .build();

                return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
        }

        private UserModel createUserFromOAuth(OAuth2User oauthUser, String email) {

                String name = oauthUser.getAttribute("name");
                String picture = oauthUser.getAttribute("picture");

                return UserModel.builder()
                                .email(email)
                                .fullName(name)
                                .profileImage(picture)
                                .password(passwordEncoder.encode("tempPassword"))
                                .role(Role.USER)
                                .isActive(true)
                                .maxRoutines(1)
                                .subscription(createFreeSubscription())
                                .build();
        }

        private FreeSubscriptionModel createFreeSubscription() {
                return FreeSubscriptionModel.builder()
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusYears(1))
                                .build();
        }
}