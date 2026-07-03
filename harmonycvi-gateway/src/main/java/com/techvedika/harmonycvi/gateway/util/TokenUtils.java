package com.techvedika.harmonycvi.gateway.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class TokenUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateToken() {
        // use UUID + random bytes for token entropy
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return UUID.randomUUID().toString() + "-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
