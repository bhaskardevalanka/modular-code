package com.techvedika.harmonycvi.gateway.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

@Configuration
public class MFAConfig {

    @Bean
    public SecretGenerator secretGenerator() {
        return new DefaultSecretGenerator();
    }

    @Bean
    public CodeGenerator codeGenerator() {
        return new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
    }

    @Bean
    public CodeVerifier codeVerifier(CodeGenerator codeGenerator, TimeProvider timeProvider) {
        return new DefaultCodeVerifier(codeGenerator, timeProvider);
    }
    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }
    
    @Bean
    public QrGenerator qrGenerator() {
        // Using Zxing PNG QR generator (commonly used for Spring Boot)
        return new ZxingPngQrGenerator();
    }
}
