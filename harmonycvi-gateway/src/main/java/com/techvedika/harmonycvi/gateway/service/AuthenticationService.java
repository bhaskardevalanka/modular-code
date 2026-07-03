package com.techvedika.harmonycvi.gateway.service;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

	JSONObject getMfaStatus(HttpServletRequest request);

	JSONObject setupTotp(HttpServletRequest request);

	ResponseEntity<Map<String, Object>> enableMfa(JSONObject request, HttpServletResponse response, String userAgent);

}
