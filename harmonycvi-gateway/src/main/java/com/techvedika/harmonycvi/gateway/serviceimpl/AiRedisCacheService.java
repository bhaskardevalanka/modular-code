package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.util.EmailService;

@Service
public class AiRedisCacheService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AiRedisCacheService.class);

	@Value("${ai.study-url}")
	private String aiStudyURL;
	
	@Value("${developers.emails}")
    private String developerEmails;
	
	protected final RestTemplate restTemplate;
	protected final ObjectMapper objectMapper;
	private EmailService emailService;

    protected AiRedisCacheService(RestTemplate restTemplate,ObjectMapper objectMapper,EmailService emailService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }
    
    public void clearAIRedicsCache() {

		try {
            LOG.info("Clearing redis cache AI server...");

			var headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", CommonConstants.APPLICATION_JSON);
            headers.set("Accept", CommonConstants.APPLICATION_JSON);
            
            var entity = new org.springframework.http.HttpEntity<>(headers);
            LOG.info("Clearing redis cache AI server...Trigerring API call");
            var response = restTemplate.exchange(aiStudyURL+ "clearRedis",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            LOG.info("Response from redis cache API call ::{}" , response.getStatusCode());

			
		} catch (Exception e) {
            LOG.error("Error while clearing redis cache AI server", e);

		}		
	}
    
    public boolean checkAIProcessorsStatus() {
		try {
            LOG.info("Checking if any AI processors are currently running ...");

			var headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", CommonConstants.APPLICATION_JSON);
            headers.set("Accept", CommonConstants.APPLICATION_JSON);
            
            var entity = new org.springframework.http.HttpEntity<>(headers);
            LOG.info("Checking if any AI processors are currently running ...AI API call trigerred");
            var response = restTemplate.exchange(aiStudyURL+ "aiMachineStatus",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                LOG.info("Checking if any AI processors are currently running ...AI response :{}" ,response.getBody() );
                JsonNode json = objectMapper.readTree(response.getBody());
                LOG.info("Checking if any AI processors are currently running ...JSON response :{}" , json );

                if("201".equalsIgnoreCase(json.path("status").asText())) {
                	String body = errorMailBody(response.getBody(),"aiMachineStatus");
                	notifyDevelopers(body);
                	return true;
                }else {
                	return "true".equalsIgnoreCase(json.path("ai_status").asText());
                }
                
            } else {
            	return false;
            }
			
		} catch (Exception e) {
            LOG.error("Error while stopping AI server", e);
			return false;
		}
		
	}
	
	public String errorMailBody(String response,String issueAPI) {
		String emailBody = "<html><body><p>Hi Developers,</p>";
		emailBody += "<p> There is some issue with "+ issueAPI+". Here is the response :"+response+"<p>";
		emailBody += "<p>Thanks & Regards,";
		emailBody += "<br>Support Team</p>";

		emailBody += "</body></html>";
		return emailBody;
	}
	
	public void notifyDevelopers(String body) {
        List<String> emails = getDeveloperEmails();
        for (String email : emails) {
            // Use your email sending logic here
        	emailService.sendEmail(email, UserConstants.REGISTRACTION, body);
            LOG.info("Sending error notification to {}" , email);
        }
    }

    public List<String> getDeveloperEmails() {
        return Arrays.asList(developerEmails.split(","));
    }
    
    public boolean isReady() {

        LOG.info("Checking AI service readiness");

        try {
            String statusUrl = aiStudyURL + "checkStatus";
            LOG.info("Calling AI service status API: {}", statusUrl);

            ResponseEntity<String> response =
                    restTemplate.getForEntity(statusUrl, String.class);

            LOG.debug("AI status API HTTP code: {}",
                      response.getStatusCode().value());
            LOG.debug("AI status API response body: {}",
                      response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                LOG.warn("AI service readiness check failed with HTTP status: {}",
                         response.getStatusCode().value());
                return false;
            }

            JSONObject json =
                    (JSONObject) new JSONParser().parse(response.getBody());

            String statusCode = String.valueOf(json.get("statusCode"));
            boolean ready = "1000".equals(statusCode);

            LOG.info("AI service readiness result: statusCode={}, ready={}",
                     statusCode, ready);

            return ready;

        } catch (Exception e) {
            LOG.error("Error while checking AI service readiness", e);
            return false;
        }
    }

    public void processRequest(JSONObject resObj) {

        String method = resObj.get("method").toString();
        String url =
                resObj.get("url").toString().replace("\\/", "/");

        LOG.info("Processing AI request: method={}, url={}", method, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if ("GET".equalsIgnoreCase(method)) {

            LOG.info("Sending GET request to AI service");
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            String.class
                    );

            LOG.debug("AI GET response HTTP code: {}",
                      response.getStatusCode().value());
            LOG.debug("AI GET response body: {}",
                      response.getBody());

        } else {

            String requestBody = String.valueOf(resObj.get("request"));
            LOG.debug("AI POST request body: {}", requestBody);

            HttpEntity<String> entity =
                    new HttpEntity<>(requestBody, headers);

            LOG.info("Sending POST request to AI service");
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            LOG.debug("AI POST response HTTP code: {}",
                      response.getStatusCode().value());
            LOG.debug("AI POST response body: {}",
                      response.getBody());
        }

        LOG.info("AI request processed successfully");

    }
}
