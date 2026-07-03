package com.techvedika.harmonycvi.gateway.dicomweb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.serviceimpl.PacsTokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DicomWebClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    
    @Autowired
	private CommonMethod commonMethod;
    
    @Autowired
	PacsTokenService pacsTokenService;

    public DicomWebClient(ObjectMapper mapper) {
        this.mapper = mapper;
        this.restTemplate = new RestTemplate();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        // Add basic auth if your PACS needs it:
        // headers.setBasicAuth("user", "password");
        return headers;
    }

    /** Get Study metadata using QIDO-RS 
     * @param authorization **/
    public List<JsonNode> getStudyByUID(String studyInstanceUID, Long orgId, String authorization) {
    	String pacsUrl = commonMethod.getPacsUrl(orgId.toString());
        String url = String.format("%s/studies?StudyInstanceUID=%s", pacsUrl, studyInstanceUID);
        System.out.println("dicomweburl::"+url);
        try {
        	
        HttpHeaders headers = new HttpHeaders();
        String token = pacsTokenService.getToken(String.valueOf(orgId));
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers); 
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
        );

        
            return mapper.readValue(response.getBody(), new TypeReference<List<JsonNode>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing study response", e);
        }
    }
    
//    /** Get Study metadata using QIDO-RS 
//     * @throws Exception **/
//    public List<JsonNode> getStudyByUID(List<String> studyInstanceUIDs, Long orgId) throws Exception {
//    	if (studyInstanceUIDs == null || studyInstanceUIDs.isEmpty()) {
//            return Collections.emptyList();
//        }
//    	String pacsUrl = commonMethod.getPacsUrl(orgId.toString());
//        // Join all UIDs with commas
//        String uidsParam = studyInstanceUIDs.stream()
//                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
//                .collect(Collectors.joining(","));
//
//        String url = String.format("%s/studies?StudyInstanceUID=%s&includefield=all",
//                pacsUrl, uidsParam);
//        System.out.println("dicomweburl::"+url);
//        String userEmailId = SecurityUtil.currentUserEmailId();
////		String token = keycloakTokenService.getUserAccessToken(userEmailId,"Techv1@3");
////		HttpHeaders headers = new HttpHeaders();
////		headers.setBearerAuth(token);
//        ResponseEntity<String> response = restTemplate.exchange(
//                url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class
//        );
//
////        String token = keycloakTokenService.getUserAccessToken(userEmailId, "Techv1@3");
////        System.out.println("token-------------------"+token);
////
////        HttpHeaders headers = new HttpHeaders();
////        headers.setBearerAuth(token); // adds "Authorization: Bearer <token>" header
////        headers.setAccept(List.of(MediaType.valueOf("application/dicom+json")));
////
////        HttpEntity<Void> entity = new HttpEntity<>(headers); // no body for GET request
////
////        ResponseEntity<String> response = restTemplate.exchange(
////                url,
////                HttpMethod.GET,
////                entity,
////                String.class
////        );
//
//        try {
//            return mapper.readValue(response.getBody(), new TypeReference<List<JsonNode>>() {});
//        } catch (Exception e) {
//            throw new RuntimeException("Error parsing study response", e);
//        }
//    }
    
    /** Get Study metadata using QIDO-RS 
     * @throws Exception **/
    public List<JsonNode> getStudyByUID(List<String> studyInstanceUIDs, String pacsUrl, String token) throws Exception {
    	System.out.println("keycloak token-3-------------------"+token);
        if (studyInstanceUIDs == null || studyInstanceUIDs.isEmpty()) {
            return Collections.emptyList();
        }

        String uidsParam = studyInstanceUIDs.stream()
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                .collect(Collectors.joining(","));

        String url = String.format("%s/studies?StudyInstanceUID=%s&includefield=all", pacsUrl, uidsParam);
        System.out.println("dicomweburl::" + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // <-- Correct
        headers.setAccept(List.of(MediaType.valueOf("application/dicom+json")));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        try {
            return mapper.readValue(response.getBody(), new TypeReference<List<JsonNode>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing study response", e);
        }
    }

    /** Get Series list for a Study **/
    public List<JsonNode> getSeriesByStudyUID(String studyInstanceUID,Long orgId) {
    	String pacsUrl = commonMethod.getPacsUrl(orgId.toString());
        String url = String.format("%s/studies/%s/series", pacsUrl, studyInstanceUID);
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class
        );

        try {
            return mapper.readValue(response.getBody(), new TypeReference<List<JsonNode>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing series response", e);
        }
    }
    
    public Map<String, Object> getFirstSeriesForStudy(String studyInstanceUID,Long orgId) throws Exception {
        // Construct URL with limit=1
    	String pacsUrl = commonMethod.getPacsUrl(orgId.toString());
        String url = String.format(
            "%s/series?StudyInstanceUID=%s&includefield=all&limit=1",
            pacsUrl, URLEncoder.encode(studyInstanceUID, StandardCharsets.UTF_8)
        );
        
        System.out.println("seriesurl:"+url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Prefer", "count");
        String token = pacsTokenService.getToken(orgId.toString());
		headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.valueOf("application/dicom+json")));

        HttpEntity<Void> entity = new HttpEntity<>(headers); 

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );


        // Parse the JSON response
        List<JsonNode> seriesList = new ArrayList<>();
        try {
            seriesList = mapper.readValue(
                response.getBody(),
                new TypeReference<List<JsonNode>>() {}
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Get total number of series from response header
        String totalSeriesHeader = response.getHeaders().getFirst("X-Total-Count");
        int totalSeriesCount = totalSeriesHeader != null ? Integer.parseInt(totalSeriesHeader) : 0;

        // Pick the first series (if any)
        JsonNode firstSeries = seriesList.isEmpty() ? null : seriesList.get(0);

        // Prepare result map
        Map<String, Object> result = new HashMap<>();
        result.put("studyInstanceUID", studyInstanceUID);
        result.put("seriesCount", totalSeriesCount);
        result.put("firstSeries", firstSeries);

        return result;
    }
}

