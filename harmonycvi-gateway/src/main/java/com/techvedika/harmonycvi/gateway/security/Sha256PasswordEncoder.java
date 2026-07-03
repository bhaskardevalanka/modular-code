package com.techvedika.harmonycvi.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Sha256PasswordEncoder implements PasswordEncoder {
	
	@Autowired
	private JwtUtils jwtUtils;

    @Override
    public String encode(CharSequence rawPassword) {
        return jwtUtils.getSHA(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return jwtUtils.getSHA(rawPassword.toString()).equals(encodedPassword);
    }

    
}