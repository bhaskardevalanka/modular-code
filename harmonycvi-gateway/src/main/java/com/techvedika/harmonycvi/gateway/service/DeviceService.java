package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;

import com.techvedika.harmonycvi.gateway.entity.DeviceDetails;

public interface DeviceService {
	public JSONObject create(JSONObject jsonRequest);

	public JSONObject update(JSONObject json);

	public JSONObject getList(JSONObject json);

	public JSONObject getById(String id);

	public DeviceDetails findById(long id);

	public void save(DeviceDetails device);

	public void update(DeviceDetails device);

	public void delete(DeviceDetails device);

	public Long getAllActiveDeviceByOrg(Long orgId);

	public Long getAllActiveDevice();
}
