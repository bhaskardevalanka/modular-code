package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;

public interface CenterService {
	public JSONObject create(JSONObject jsonRequest);
	public JSONObject update(JSONObject json);
	public JSONObject getList(JSONObject json);
	public JSONObject getById(String id);
}
