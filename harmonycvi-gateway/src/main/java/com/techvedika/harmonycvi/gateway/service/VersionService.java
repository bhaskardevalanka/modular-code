package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;

import jakarta.servlet.http.HttpServletRequest;

public interface VersionService {
	JSONObject checkVersion(HttpServletRequest request);
}
