package com.techvedika.harmonycvi.gateway.pacsproxy;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.techvedika.harmonycvi.gateway.dto.StudyDTO;
import com.techvedika.harmonycvi.gateway.util.JsonUtils;

@Service
@Transactional
public class PacsProxySeriesServiceImpl implements PacsProxySeriesService {

	@Value("${dcm4cheeBaseUrl}")
	private String dcm4cheeBaseUrl; // e.g. http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final WebClient webClient;

	@Autowired
	public PacsProxySeriesServiceImpl(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl(dcm4cheeBaseUrl).build();
	}	

	@Override
	public StudyDTO fetchSeriesByStudyUID(String studyUID) {
		try {
			/// studies/{studyUID}/series:
			String url = dcm4cheeBaseUrl + "/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8)
					+ "/series";

			// Setting up the headers (if needed)s
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer your_token_here"); // Example of header for auth
			HttpEntity<String> entity = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			// return JsonUtils.parseJsonString(response.getBody());

			// Convert the response to StudyDTO
			return mapToStudyDTO(response.getBody());

		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch study metadata", ex);
		}
	}

	@Override
	public JSONObject fetchSeriesByStudyAndSeriesUID(String studyUID, String seriesUID) {
		try {
			String url = dcm4cheeBaseUrl + "/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8)
					+ "/series/" + URLEncoder.encode(seriesUID, StandardCharsets.UTF_8);
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return JsonUtils.parseJsonString(response.getBody());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch study metadata", ex);
		}
	}

	@Override
	public JSONObject fetchSeriesMetadata(String studyUID, String seriesUID) {
		try {
			String url = dcm4cheeBaseUrl + "/studies/" + URLEncoder.encode(studyUID, StandardCharsets.UTF_8)
					+ "/series/" + URLEncoder.encode(seriesUID, StandardCharsets.UTF_8) + "/metadata";
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			return JsonUtils.parseJsonString(response.getBody());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch study metadata", ex);
		}
	}

	// Method to convert raw response to StudyDTO
	private StudyDTO mapToStudyDTO(String responseBody) {
		// Assuming the response body is in JSON format, you can use a JSON library to
		// parse it
		JSONObject jsonResponse;
		StudyDTO studyDTO = new StudyDTO();
		try {
			jsonResponse = JsonUtils.parseJsonString(responseBody);
			studyDTO.setStudyInstanceUID(JsonUtils.getJsonFieldValue(jsonResponse, "studyInstanceUID"));
			studyDTO.setStudyID(JsonUtils.getJsonFieldValue(jsonResponse, "studyID"));
			studyDTO.setStudyDate(JsonUtils.getJsonFieldValue(jsonResponse, "studyDate"));
			studyDTO.setStudyTime(JsonUtils.getJsonFieldValue(jsonResponse, "studyTime"));
			studyDTO.setAccessionNumber(JsonUtils.getJsonFieldValue(jsonResponse, "accessionNumber"));
			studyDTO.setAdmissionID(JsonUtils.getJsonFieldValue(jsonResponse, "admissionID"));
			studyDTO.setStudyDescription(JsonUtils.getJsonFieldValue(jsonResponse, "studyDescription"));
			studyDTO.setStatus(JsonUtils.getJsonFieldValue(jsonResponse, "status", "Pending"));
			studyDTO.setNoOfImages(JsonUtils.getJsonFieldValue(jsonResponse, "noOfImages", "0"));
			studyDTO.setStudyCustomAttribute1(JsonUtils.getJsonFieldValue(jsonResponse, "studyCustomAttribute1", null));
			studyDTO.setStudyCustomAttribute2(JsonUtils.getJsonFieldValue(jsonResponse, "studyCustomAttribute2", null));
			studyDTO.setStudyCustomAttribute3(JsonUtils.getJsonFieldValue(jsonResponse, "studyCustomAttribute3", null));
			studyDTO.setAccessControlID(JsonUtils.getJsonFieldValue(jsonResponse, "accessControlID", "*"));
			studyDTO.setRejectionState(JsonUtils.getJsonFieldValue(jsonResponse, "rejectionState", "Unknown"));
			studyDTO.setExpirationState(JsonUtils.getJsonFieldValue(jsonResponse, "expirationState", "Unknown"));
			studyDTO.setExpirationDate(JsonUtils.getJsonFieldValue(jsonResponse, "expirationDate", "*"));
			studyDTO.setExpirationExporterID(JsonUtils.getJsonFieldValue(jsonResponse, "expirationExporterID", null));
			studyDTO.setExternalRetrieveAET(JsonUtils.getJsonFieldValue(jsonResponse, "externalRetrieveAET", null));
			studyDTO.setSize(JsonUtils.getJsonFieldValueForLong(jsonResponse, "size", -1L));
			studyDTO.setReferringPhysicianName(
					JsonUtils.getJsonFieldValue(jsonResponse, "referringPhysicianName", null));
			studyDTO.setIsAIProccessed(JsonUtils.getJsonFieldValueForBoolean(jsonResponse, "isAIProccessed", false));
			studyDTO.setQflowStatus(JsonUtils.getJsonFieldValue(jsonResponse, "qflowStatus", null));
			studyDTO.setVentricleAssessmentStatus(
					JsonUtils.getJsonFieldValue(jsonResponse, "ventricleAssessmentStatus", null));
			studyDTO.setClassificationStatus(JsonUtils.getJsonFieldValue(jsonResponse, "classificationStatus", null));
			studyDTO.setOrgId(JsonUtils.getJsonFieldValueForLong(jsonResponse, "orgId", -1L));
			studyDTO.setDicomImagesCount(JsonUtils.getJsonFieldValueForLong(jsonResponse, "dicomImagesCount", 0L));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return studyDTO;
	}
}
