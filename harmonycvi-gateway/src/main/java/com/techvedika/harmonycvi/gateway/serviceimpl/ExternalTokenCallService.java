package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.KeycloakToken;
import com.techvedika.harmonycvi.gateway.entity.OrgApiConfig;
import com.techvedika.harmonycvi.gateway.repository.OrgApiConfigRepository;

@Service
public class ExternalTokenCallService {

    @Autowired
    private OrgApiConfigRepository repo;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    public KeycloakToken callExternalApiAndGetToken(Long orgId) throws Exception {

        // 1. Fetch config from DB
        OrgApiConfig config = repo.findByOrgId(orgId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new Exception("No API config found for orgId " + orgId));

        String url = config.getApiUrl();
        
        if(url.isBlank() || url.isEmpty()) {
        	throw new Exception("No API config found for orgId " + orgId);
        }
        
        String method = config.getMethod().toUpperCase();
        String requestType = config.getRequestParamsType();
        String responseTokenField = config.getResponseTokenField();

        // ===============================
        // 2. Build Headers dynamically
        // ===============================
        HttpHeaders headers = new HttpHeaders();

        if (config.getHeaders() != null) {
            JsonNode node = mapper.readTree(config.getHeaders());
            node.fields().forEachRemaining(e -> headers.add(e.getKey(), e.getValue().asText()));
        }

        // Default content-type / optional
        if (!headers.containsKey("Content-Type")) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        // ===============================
        // 3. Build Request Body based on request_params_type
        // ===============================
        Object requestBody = null;

        if (config.getRequestParams() != null) {
            JsonNode paramNode = mapper.readTree(config.getRequestParams());

            switch (requestType) {

                case "FORM_URL_ENCODED":
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                    paramNode.fields().forEachRemaining(e -> form.add(e.getKey(), e.getValue().asText()));
                    requestBody = form;
                    break;

                case "JSON":
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    requestBody = mapper.readValue(config.getRequestParams(), Map.class);
                    break;

                case "QUERY_STRING":
                    // append params to URL
                    StringBuilder sb = new StringBuilder(url).append("?");
                    paramNode.fields().forEachRemaining(e -> {
                        sb.append(e.getKey())
                          .append("=")
                          .append(e.getValue().asText())
                          .append("&");
                    });
                    url = sb.substring(0, sb.length() - 1);
                    break;

                default:
                    throw new Exception("Unsupported requestParamsType: " + requestType);
            }
        }

        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

        // ===============================
        // 4. Execute API Call
        // ===============================
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(method),
                entity,
                String.class
        );

        String responseBody = response.getBody();

        // ===============================
        // 5. Extract token dynamically
        // ===============================
        JsonNode jsonResponse = mapper.readTree(responseBody);

        System.out.println("jsonRepsonseese:"+jsonResponse);
        if (!jsonResponse.has(responseTokenField)) {
            throw new Exception("Token field '" + responseTokenField + "' not found in response.");
        }

        return null;
    }
}

