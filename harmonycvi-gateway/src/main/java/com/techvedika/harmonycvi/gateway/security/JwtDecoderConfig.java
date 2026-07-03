package com.techvedika.harmonycvi.gateway.security;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import jakarta.xml.bind.DatatypeConverter;

/**
 * Provides a {@link JwtDecoder} bean backed by the application's HMAC-SHA256 secret.
 *
 * <p>Spring Boot's OAuth2 Resource Server auto-configuration only creates a
 * {@code JwtDecoder} when {@code spring.security.oauth2.resourceserver.jwt.*}
 * properties are present, which this app does not use (it manages its own JWT
 * signing via {@link JwtUtils}).  This configuration fills that gap so that
 * beans such as {@code PacsProxyStudyServiceImpl} — which use the standard
 * Spring Security {@code JwtDecoder} interface — can be injected correctly.</p>
 *
 * <p>Note: when Keycloak is disabled ({@code keycloak.enabled=false}), PACS
 * token requests return an empty string and the decoder is never actually
 * invoked.  This bean is required purely for application-context startup.</p>
 */
@Configuration
public class JwtDecoderConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Decode the Base64-encoded secret — same approach used in JwtUtils.generateToken()
        byte[] keyBytes = DatatypeConverter.parseBase64Binary(jwtSecret);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
