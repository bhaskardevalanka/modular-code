package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

public interface OrganizationService {
public JSONObject create(JSONObject json, HttpServletRequest request);
	
	public JSONObject update(JSONObject json);
	
	public JSONObject delete(JSONObject json);
	
	public JSONObject getList();

	public JSONObject getOrgById(JSONObject json);

	public JSONObject getAll();

	JSONObject getOrgList();
	
	JSONObject isExternalOrg(String orgId);
	
	ResponseEntity<JSONObject> getPacsUrl(JSONObject orgId);

	JSONObject getOrgListByEmail(JSONObject json);

}
