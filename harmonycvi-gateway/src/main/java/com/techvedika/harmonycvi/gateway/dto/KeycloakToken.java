package com.techvedika.harmonycvi.gateway.dto;

import java.time.Instant;

public class KeycloakToken {
    private String accessToken;
    private long expiresIn;      // seconds
    private Instant expiryTime;

    public KeycloakToken(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.expiryTime = Instant.now().plusSeconds(expiresIn);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isExpired() {
        // 30 sec buffer
        return Instant.now().isAfter(expiryTime.minusSeconds(30));
    }

	public Instant getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(Instant expiryTime) {
		this.expiryTime = expiryTime;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
    
}
