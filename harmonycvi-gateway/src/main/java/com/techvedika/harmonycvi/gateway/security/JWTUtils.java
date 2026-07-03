package com.techvedika.harmonycvi.gateway.security;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

@Component
public class JWTUtils {
	
//	@Autowired
//    private UserUtils userUtils;
//	
//    private Key signingKey;
//    private String secret;
//
//    /** Initialize signing key once at startup */
//    @PostConstruct
//    public void initKey() {    	
//    	secret = userUtils.getPropertyValue(UserConstants.JWT_SECKET_KEY);        
//        if (secret == null || secret.isEmpty()) {
//            SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//            signingKey = key;
//            System.out.println("Generated Secret: " + Base64.getEncoder().encodeToString(key.getEncoded()));
//        } else {
//            byte[] keyBytes = Base64.getDecoder().decode(secret);
//            signingKey = Keys.hmacShaKeyFor(keyBytes);
//            
//            //
//            
////            byte[] keyBytes = Base64.getDecoder().decode(secret);
////            signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
//        }
//        
//        
//        
//    }
//
//    /**
//     * Create H‑S256 JWT.
//     *
//     * @param id         userId or token id
//     * @param issuer     e.g. "DICOM"
//     * @param subject    e.g. "DICOM‑WEB"
//     * @param ttlMillis  time‑to‑live in ms
//     */
//    public String createJWT(String id,
//                            String issuer,
//                            String subject,
//                            long ttlMillis) {
//
//        long nowMillis = System.currentTimeMillis();
//        Date now = new Date(nowMillis);
//
//        JwtBuilder builder = Jwts.builder().setId(id).setIssuer(issuer).setSubject(subject).setIssuedAt(now).signWith(signingKey, SignatureAlgorithm.HS256);
//
//        if (ttlMillis > 0) {
//            builder.setExpiration(new Date(nowMillis + ttlMillis));
//        }
//        return builder.compact();
//    }
//
//    /**
//     * Decode JWT and never throw — caller decides what to do with status.
//     *
//     * Returns map keys:
//     *  • id      – token ID / user ID (when valid)  
//     *  • status  – "success" | "expired" | "invalid"
//     */
//    public Map<String, Object> decodeJWT(String jwt) {
//        Map<String, Object> map = new HashMap<>();
//        try {
//            Claims claims = Jwts.parserBuilder()
//                                .setSigningKey(signingKey)
//                                .build()
//                                .parseClaimsJws(jwt)
//                                .getBody();
//            map.put("id", claims.getId());
//            map.put("status", "success");
//        } catch (ExpiredJwtException e) {
//            map.put("status", "expired");
//        } catch (MalformedJwtException | SignatureException |
//                 UnsupportedJwtException | IllegalArgumentException e) {
//            map.put("status", "invalid");
//        }
//        return map;
//    }
}