package com.techvedika.harmonycvi.gateway.security;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.DatatypeConverter;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private String jwtExpirationMs;
	
	@PostConstruct
	public void initKey() {
//		jwtSecret = userUtils.getPropertyValue(UserConstants.JWT_SECKET_KEY);
//		jwtExpirationMs = userUtils.getPropertyValue(UserConstants.JWT_EXPIRE_HRS_MILLIS);
	}

	public String generateToken(String email) {

		// String jwtSecret = userUtils.getPropertyValue(UserConstants.JWT_SECKET_KEY);

		// String jwtExpirationMs =
		// userUtils.getPropertyValue(UserConstants.JWT_EXPIRE_HRS_MILLIS);

		long expirationMillis = Long.parseLong(jwtExpirationMs);
		; // make sure this is a long

		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationMillis);

		// The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		// We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtSecret);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		return Jwts.builder().setSubject(email).setIssuedAt(now).setExpiration(expiryDate)
				.signWith(signatureAlgorithm, signingKey).compact();
	}

	public String getEmailFromJwt(String token) {
		Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
		String id = claims.getId(); // gets 'jti'
		String subject = claims.getSubject(); // gets 'sub'
		String issuer = claims.getIssuer(); // gets 'iss'
		return subject;
	}

	public boolean validateJwtToken(String token) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			// log or handle invalid token exceptions
		}
		return false;
	}

	public String getSHA(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			StringBuilder hashtext = new StringBuilder(no.toString(16));
			while (hashtext.length() < 64) {
				hashtext.insert(0, "0");
			}
			return hashtext.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error while hashing password", e);
		}
	}
}