package com.fitapp.backend.infrastructure.security.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
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

import com.fitapp.backend.infrastructure.security.auth.converter.JwtAuthConverter;
import com.fitapp.backend.infrastructure.security.auth.handler.OAuth2SuccessHandler;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${jwt.secret}")
        private String jwtSecret;

        private final JwtAuthConverter jwtAuthConverter;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final UserDetailsService userDetailsService;
        private final RSAPrivateKey privateKey;
        private final RSAPublicKey publicKey;

        public SecurityConfig(JwtAuthConverter jwtAuthConverter,
                        @Lazy OAuth2SuccessHandler oAuth2SuccessHandler,
                        UserDetailsService userDetailsService, RSAPrivateKey privateKey,
                        RSAPublicKey publicKey) throws IOException, GeneralSecurityException {
                this.jwtAuthConverter = jwtAuthConverter;
                this.oAuth2SuccessHandler = oAuth2SuccessHandler;
                this.userDetailsService = userDetailsService;
                this.privateKey = privateKey;
                this.publicKey = publicKey;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/api-docs/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/actuator/health",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/premium/**").hasAnyRole("PREMIUM_USER", "ADMIN")
                                                .requestMatchers("/api/coach/**").hasAnyRole("COACH", "ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(oAuth2SuccessHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .userDetailsService(userDetailsService);

                return http.build();
        }
        
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public JwtDecoder jwtDecoder() throws Exception {
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

                configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", 
                                                                "https://appfit.prod", 
                                                                "http://10.0.2.2:8080", 
                                                                "http://localhost:8080"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
                configuration.setExposedHeaders(Arrays.asList("X-Auth-Token", "Authorization"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}