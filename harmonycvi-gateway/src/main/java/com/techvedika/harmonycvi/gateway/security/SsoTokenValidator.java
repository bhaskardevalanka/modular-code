package com.techvedika.harmonycvi.gateway.security;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techvedika.harmonycvi.gateway.entity.OrgApiConfig;
import com.techvedika.harmonycvi.gateway.projection.OrgPacsValidationUrlProjection;
import com.techvedika.harmonycvi.gateway.repository.OrgApiConfigRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;

import org.springframework.http.*;

@Service
public class SsoTokenValidator {
    private static final Logger LOG = LoggerFactory.getLogger(SsoTokenValidator.class);

	
	@Autowired
    private OrganizationRepository orgRepo;
	
//	@Value("${acton.validate_url}")
//	String url;
//	@Value("${acton.method}")
//	String method;
//	@Value("${acton.request_type}")
//	String requestType;
//	@Value("${acton.request_headers}")
//	String requestHeaders;
//	@Value("${acton.request_params}")
//	String requestParams;
	
	@Autowired
    private OrgApiConfigRepository repo;
	
	@Autowired
    private ObjectMapper mapper;

    private final RestTemplate restTemplate = new RestTemplate();

    public JsonNode validateToken(String token, String orgId) {

        LOG.info("Starting token validation for orgId: {}", orgId);
        LOG.info("Token received. Length: {}", token != null ? token.length() : 0);

        try {
            Optional<OrgPacsValidationUrlProjection> orgOpt =
                    orgRepo.findPacsValidationUrlById(Long.valueOf(orgId));

            if (orgOpt.isEmpty()) {
                LOG.error("No SSO configuration found for orgId: {}", orgId);
                throw new IllegalArgumentException("No SSO configuration found for org: " + orgId);
            }

            String validationUrl = orgOpt.get().getValidationUrl();
            LOG.info("Validation URL resolved for orgId {}: {}", orgId, validationUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            LOG.info("Calling SSO validation endpoint...");
            ResponseEntity<String> response = restTemplate.exchange(
                    validationUrl, HttpMethod.GET, entity, String.class);

            LOG.info("SSO validation response status: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                LOG.info("SSO validation successful. Fetching user info...");
                JsonNode userInfo = fetchUserInfo(token, validationUrl);
                LOG.info("User info fetched successfully");
                return userInfo;
            }

            LOG.warn("SSO validation failed. Status: {}", response.getStatusCode());
            return null;

        } catch (Exception ex) {
            LOG.error("Exception during token validation for orgId: {}", orgId, ex);
            return null;
        }
    }
    public JsonNode fetchUserInfo(String token, String userInfoUrl) {

        LOG.info("Fetching user info from URL: {}", userInfoUrl);

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

            LOG.info("User info response status: {}", response.getStatusCode());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());

            LOG.info("User info JSON parsed successfully");
            return node;

        } catch (Exception e) {
            LOG.error("Exception while fetching user info from URL: {}", userInfoUrl, e);
            return null;
        }
    }

    public JsonNode callExternalApiAndGetToken(String token, String orgId) throws Exception {

        LOG.info("Starting external API validation for orgId: {}", orgId);
        LOG.info("Token length: {}", token != null ? token.length() : 0);

        // 1. Fetch config
        OrgApiConfig config = repo.findByOrgId(Long.valueOf(orgId))
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    LOG.error("No API config found for orgId: {}", orgId);
                    return new Exception("No API config found for orgId " + orgId);
                });

        String url = config.getUserApiUrl();
        String method = config.getUserMethod().toUpperCase();
        String requestType = config.getUserRequestParamsType();
        String userResponseTokenField = config.getUserResponseTokenField();

        LOG.info("API Config Loaded - URL: {}, Method: {}, RequestType: {}, ResponseField: {}",
                url, method, requestType, userResponseTokenField);

        // 2. Headers
        HttpHeaders headers = new HttpHeaders();

        if (config.getUserHeaders() != null) {
            LOG.info("Parsing dynamic headers from config");
            JsonNode node = mapper.readTree(config.getUserHeaders());
            node.fields().forEachRemaining(e -> headers.add(e.getKey(), e.getValue().asText()));
        }

        if (!headers.containsKey("Content-Type")) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        Object requestBody = null;

        // 3. Build request body
        if (config.getUserRequestParams() != null) {

            LOG.info("Building request parameters. Type: {}", requestType);

            JsonNode paramNode = mapper.readTree(config.getUserRequestParams());

            if (paramNode instanceof ObjectNode) {
                ((ObjectNode) paramNode).put("token", token);
            }

            switch (requestType) {

                case "FORM_URL_ENCODED":
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                    paramNode.fields().forEachRemaining(e ->
                            form.add(e.getKey(), e.getValue().asText()));

                    requestBody = form;
                    LOG.info("Form URL Encoded body prepared");
                    break;

                case "JSON":
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    requestBody = mapper.convertValue(paramNode, Map.class);
                    LOG.info("JSON request body prepared");
                    break;

                case "QUERY_STRING":
                    StringBuilder sb = new StringBuilder(url).append("?");
                    paramNode.fields().forEachRemaining(e ->
                            sb.append(e.getKey())
                              .append("=")
                              .append(e.getValue().asText())
                              .append("&"));

                    url = sb.substring(0, sb.length() - 1);
                    LOG.info("Query string appended to URL: {}", url);
                    break;

                default:
                    LOG.error("Unsupported requestParamsType: {}", requestType);
                    throw new Exception("Unsupported requestParamsType: " + requestType);
            }
        }

        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

        // 4. Execute call
        LOG.info("Calling external API: {}", url);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(method),
                entity,
                String.class
        );

        LOG.info("External API response status: {}", response.getStatusCode());

        String responseBody = response.getBody();
        JsonNode jsonResponse = mapper.readTree(responseBody);

        LOG.info("External API response parsed successfully");

        // 5. Validate token field
        if (!jsonResponse.has(userResponseTokenField)) {
            LOG.error("Token field '{}' not found in response", userResponseTokenField);
            throw new Exception("Token field '" + userResponseTokenField + "' not found in response.");
        }

        boolean isValid = jsonResponse.get(userResponseTokenField).asBoolean();
        LOG.info("Token validation field value: {}", isValid);

        if (isValid) {
            LOG.info("External token validation SUCCESS for orgId: {}", orgId);
            return jsonResponse;
        }

        LOG.warn("External token validation FAILED for orgId: {}", orgId);
        return null;
    }
}
