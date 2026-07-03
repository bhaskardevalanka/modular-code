package com.techvedika.harmonycvi.gateway.security;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.projection.AuthUserProjection;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired private UserRepository repo;

    @Override
    public org.springframework.security.core.userdetails.User loadUserByUsername(String email) {
    	AuthUserProjection u = repo.findAuthUserByEmail(email)
    	        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    	    String pass = (u.password() == null || u.password().isEmpty())
    	                  ? u.onetimePassword()
    	                  : u.password();

    	    return new org.springframework.security.core.userdetails.User(
    	        u.email(),
    	        pass,
    	        Collections.emptyList()
    	    );
    }
    
    public String getJwtTokenForUser(String email) {
    	return repo.findJwtTokenByEmail(email).orElse(null);
    }
}