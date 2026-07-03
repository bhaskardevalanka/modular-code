package com.techvedika.harmonycvi.gateway.projection;

public record ResetPasswordProjection(Long id,String email, String password, String onetimePassword,String jwtToken) {}
