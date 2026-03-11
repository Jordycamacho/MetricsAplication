package com.fitapp.backend.infrastructure.security.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import com.fitapp.backend.infrastructure.security.auth.converter.JwtAuthConverter;
import com.fitapp.backend.infrastructure.security.auth.filter.JwtBlacklistFilter;
import com.fitapp.backend.infrastructure.security.auth.handler.OAuth2SuccessHandler;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Value("${cors.allowed-origins}")
        private String allowedOrigins;

        private final JwtAuthConverter jwtAuthConverter;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final UserDetailsService userDetailsService;
        private final RSAPrivateKey privateKey;
        private final RSAPublicKey publicKey;
        private final JwtBlacklistFilter jwtBlacklistFilter;

        public SecurityConfig(JwtAuthConverter jwtAuthConverter,
                        @Lazy OAuth2SuccessHandler oAuth2SuccessHandler,
                        UserDetailsService userDetailsService,
                        RSAPrivateKey privateKey,
                        RSAPublicKey publicKey,
                        JwtBlacklistFilter jwtBlacklistFilter)
                        throws IOException, GeneralSecurityException {
                this.jwtAuthConverter = jwtAuthConverter;
                this.oAuth2SuccessHandler = oAuth2SuccessHandler;
                this.userDetailsService = userDetailsService;
                this.privateKey = privateKey;
                this.publicKey = publicKey;
                this.jwtBlacklistFilter = jwtBlacklistFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .addFilterBefore(jwtBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/login/oauth2/**",
                                                                "/oauth2/authorization/**",
                                                                "/api-docs/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/actuator/health",
                                                                "/actuator/prometheus",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/premium/**").hasAnyRole("PREMIUM_USER", "ADMIN")
                                                .requestMatchers("/api/coach/**").hasAnyRole("COACH", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/routines/**").authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/routines")
                                                .hasAnyRole("USER", "PREMIUM_USER", "COACH", "ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(oAuth2SuccessHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .userDetailsService(userDetailsService)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                return NimbusJwtDecoder.withPublicKey(publicKey).build();
        }

        @Bean
        public JwtEncoder jwtEncoder() {
                JWK jwk = new RSAKey.Builder(publicKey)
                                .privateKey(privateKey)
                                .build();
                JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
                return new NimbusJwtEncoder(jwks);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                List<String> origins = Arrays.asList(allowedOrigins.split(","));
                configuration.setAllowedOrigins(origins);

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
                configuration.setExposedHeaders(Arrays.asList("X-Auth-Token", "Authorization"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}