package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;

public interface DashBoardService {
	JSONObject getAllCount(String orgId);
	JSONObject getSuperAdmin();
	JSONObject getOrgDashboard(JSONObject json);
}
