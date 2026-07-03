package com.techvedika.harmonycvi.gateway.security;


import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private SsoTokenValidator ssoTokenValidator;

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        LOG.info("JWT Filter invoked for URI: {}", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        final String sourceApp = request.getHeader("X-Client-App");

        String email = null;
        String jwtToken = null;
        boolean isExternalApp = sourceApp != null && sourceApp.contains("EXTERNAL_APP");

        LOG.info("Source App Header: {}", sourceApp);
        LOG.info("Application Type: {}", isExternalApp ? "External App" : "Internal App");

        // Extract token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            LOG.info("Authorization header contains Bearer token");
        } else if (authHeader != null) {
            jwtToken = authHeader;
            LOG.info("Authorization header contains raw token");
        } else {
            LOG.info("Authorization header is missing");
        }

        if (jwtToken == null) {
            LOG.info("JWT token is null. Skipping authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        LOG.info("JWT token received. Length: {}", jwtToken.length());

        if (isExternalApp) {

            LOG.info("Processing External App authentication");

            String[] orgIdSplit = sourceApp.split("_");
            String orgId = orgIdSplit[orgIdSplit.length - 1];

            LOG.info("Extracted orgId from header: {}", orgId);

            JsonNode isValidSso = null;

            try {
                LOG.info("Calling SSO validator for orgId: {}", orgId);
                isValidSso = ssoTokenValidator.callExternalApiAndGetToken(jwtToken, orgId);
            } catch (Exception e) {
                LOG.error("Exception while validating SSO token for orgId: {}", orgId, e);
            }

            if (isValidSso == null) {
                LOG.info("SSO validation failed. Token invalid.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid SSO token");
                return;
            }

            LOG.info("SSO validation successful");

            String email1 = "vinay@yopmail.com";

            if (isValidSso.has("email")) {
                email1 = StringUtils.strip(isValidSso.get("email").toString(), "\"");
            }

            LOG.info("Email extracted from SSO: {}", email1);

            String firstName = email1.split("@")[0];

            com.techvedika.harmonycvi.gateway.entity.User userExists =
                    userService.ensureUserExists(email1, firstName, "", jwtToken, orgId);

            if (userExists == null) {
                LOG.error("User creation/lookup failed for email: {}", email1);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            LOG.info("User ensured in system for email: {}", email1);

            User userDetails = userDetailsService.loadUserByUsername(email1);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            LOG.info("Authentication set in SecurityContext for external user: {}", email1);

        } else {

            LOG.info("Processing Internal JWT authentication");

            if (jwtUtil.validateJwtToken(jwtToken)) {

                LOG.info("JWT validation successful");

                email = jwtUtil.getEmailFromJwt(jwtToken);
                LOG.info("Email extracted from JWT: {}", email);

                if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                    User userDetails = userDetailsService.loadUserByUsername(email);
                    String storedToken = userDetailsService.getJwtTokenForUser(email);

                    if (storedToken == null || !storedToken.equals(jwtToken)) {
                        LOG.warn("Stored token mismatch or expired for user: {}", email);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Invalid or expired token");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    LOG.info("Authentication set in SecurityContext for internal user: {}", email);
                }

            } else {
                LOG.warn("JWT validation failed for token length: {}", jwtToken.length());
            }
        }

        LOG.info("Proceeding with filter chain for URI: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
