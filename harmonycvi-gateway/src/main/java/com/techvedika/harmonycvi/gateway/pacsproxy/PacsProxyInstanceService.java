package com.techvedika.harmonycvi.gateway.pacsproxy;

import org.json.simple.JSONObject;

public interface PacsProxyInstanceService {
	JSONObject fetchInstanceByUID(String studyUID, String seriesUID, String instanceUID);
    JSONObject fetchInstanceMetadata(String studyUID, String seriesUID, String instanceUID);

}
