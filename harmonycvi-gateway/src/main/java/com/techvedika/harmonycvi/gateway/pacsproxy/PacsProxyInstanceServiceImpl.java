package com.techvedika.harmonycvi.gateway.pacsproxy;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.techvedika.harmonycvi.gateway.util.JsonUtils;

@Service
@Transactional
public class PacsProxyInstanceServiceImpl implements PacsProxyInstanceService {
	
	@Value("${dcm4cheeBaseUrl}")
    private String dcm4cheeBaseUrl;  // e.g. http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	 private JdbcTemplate jdbcTemplate;
	
	//String url = dcm4cheeBaseUrl + "/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8) + "/series/" + URLEncoder.encode(seriesUID, StandardCharsets.UTF_8) + "/instances";

	//String url = dcm4cheeBaseUrl + "/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8) + "/series/" + URLEncoder.encode(seriesUID, StandardCharsets.UTF_8) + "/instances/" + URLEncoder.encode(instanceUID, StandardCharsets.UTF_8);

	//String url = dcm4cheeBaseUrl + "/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8) + "/series/" + URLEncoder.encode(seriesUID, StandardCharsets.UTF_8) + "/instances/" + URLEncoder.encode(instanceUID, StandardCharsets.UTF_8) + "/metadata";

	@Override
	public JSONObject fetchInstanceByUID(String studyUID, String seriesUID, String instanceUID) {
		try {
        	String url = "";
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return JsonUtils.parseJsonString(response.getBody());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch study metadata", ex);
		}
	}

	@Override
	public JSONObject fetchInstanceMetadata(String studyUID, String seriesUID, String instanceUID) {
		try {
			String url = "";
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return JsonUtils.parseJsonString(response.getBody());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch study metadata", ex);
		}
	}
}
