package com.techvedika.harmonycvi.gateway.security;

import org.springframework.security.core.Authentication;
//import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
//	public static Long currentUserId() {
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		if (auth != null && auth.getPrincipal() instanceof Long) {
//			return (Long) auth.getPrincipal();
//		}
//		return null;
//	}
//
//	public static String currentJwtToken() {
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		if (auth != null && auth.getCredentials() instanceof String) {
//			return (String) auth.getCredentials(); // This is your JWT token
//		}
//		return null;
//	}
	
	public static String currentUserEmailId() {
		String email = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			email = auth.getName(); // Usually the user's email or username
			System.out.println("Authenticated user email Id: " + email);
		}
		return email;
	}
}