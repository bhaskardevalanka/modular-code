package com.techvedika.harmonycvi.gateway.projection;

public record AuthUserProjection(String email, String password, String onetimePassword) {}