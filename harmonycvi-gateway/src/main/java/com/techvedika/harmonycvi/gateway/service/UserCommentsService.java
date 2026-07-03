package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;

public interface UserCommentsService {
	public JSONObject save(JSONObject json);
	public JSONObject getByPatientId(JSONObject json);
}
