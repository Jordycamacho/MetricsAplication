package com.fitapp.backend.auth.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class JwtKeyConfig {

    @Bean
    public RSAPrivateKey rsaPrivateKey(
            @Value("${jwt.private-key-location}") Resource resource)
            throws IOException, GeneralSecurityException {

        try (InputStream is = resource.getInputStream()) {
            String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }
    }

    @Bean
    public RSAPublicKey rsaPublicKey(
            @Value("${jwt.public-key-location}") Resource resource)
            throws IOException, GeneralSecurityException {

        try (InputStream is = resource.getInputStream()) {
            String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }
    }
}