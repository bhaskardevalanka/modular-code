package com.techvedika.harmonycvi.gateway.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.exception.UnexpectedRunTimeException;
import com.techvedika.harmonycvi.gateway.serviceimpl.AiRedisCacheService;

public abstract class AbstractAiServerController implements AiServerController {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAiServerController.class);
	
    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;
    private AiRedisCacheService aiRedisCache;

    protected AbstractAiServerController(RestTemplate restTemplate,
                                         ObjectMapper objectMapper,
                                         AiRedisCacheService aiRedisCache) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.aiRedisCache = aiRedisCache;
    }

    protected abstract HttpHeaders buildHeaders();

    protected abstract String getCheckStatusApi();
    protected abstract String getStopApi();
    protected abstract String getStartApi();

    @Override
    public void handleIdleServer() {

        LOG.info("🔎 handleIdleServer() invoked");

        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            LOG.info("Calling AI check-status API: {}", getCheckStatusApi());

            ResponseEntity<String> response = restTemplate.exchange(
                    getCheckStatusApi(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            LOG.info("Check-status HTTP code: {}", response.getStatusCode().value());

            if (!response.getStatusCode().is2xxSuccessful()) {
                LOG.warn("Non-success response from check-status API: {}",
                        response.getStatusCode());
                return;
            }

            if (response.getBody() == null) {
                LOG.warn("Check-status API returned empty body");
                return;
            }

            LOG.debug("Check-status API response body: {}", response.getBody());

            JsonNode json = objectMapper.readTree(response.getBody());
            String status = json.path("status").asText("");

            LOG.info("AI server current status: {}", status);

            if (!"running".equalsIgnoreCase(status)) {
                LOG.info("AI server is not running. No action required.");
                return;
            }

            LOG.info("AI server is RUNNING. Checking processor activity...");

            boolean processorsActive = aiRedisCache.checkAIProcessorsStatus();

            LOG.info("AI processor active status: {}", processorsActive);

            if (processorsActive) {
                LOG.info("Processors are active. Skipping stop operation.");
                return;
            }

            LOG.info("Processors inactive. Clearing Redis cache and stopping server...");

            aiRedisCache.clearAIRedicsCache();

            LOG.info("Calling AI stop API: {}", getStopApi());

            ResponseEntity<String> stopResponse =
                    restTemplate.exchange(
                            getStopApi(),
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            LOG.info("Stop API HTTP code: {}", stopResponse.getStatusCode().value());

            if (!stopResponse.getStatusCode().is2xxSuccessful()) {
                LOG.error("Failed to stop AI server. HTTP code: {}",
                        stopResponse.getStatusCode());
                throw new UnexpectedRunTimeException(
                        "Failed to stop server: " + stopResponse.getStatusCode()
                );
            }

            LOG.info("AI server stop request completed successfully");

        } 
        catch (Exception e) {
            LOG.error("Error while stopping AI server", e);
            throw new UnexpectedRunTimeException("Error while stopping AI server", e);
        }
    }
    
    @Override
    public String getAIServerStatus() {

        LOG.debug("Fetching AI server status");

        try {
            HttpHeaders headers = buildHeaders();
            LOG.debug("Headers built successfully for status check");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            LOG.debug("Calling AI server status API: {}", getCheckStatusApi());

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            getCheckStatusApi(),
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            LOG.debug("Status API response code: {}", response.getStatusCode().value());
            LOG.debug("Status API response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                LOG.warn("Non-2xx response while checking AI server status: {}",
                         response.getStatusCode().value());
                return "";
            }

            JsonNode json = objectMapper.readTree(response.getBody());
            String status = json.path("status").asText("");

            LOG.info("AI server current status: {}", status);
            return status;

        } catch (Exception e) {
            LOG.error("Error while fetching AI server status", e);
            return "";
        }
    }

    @Override
    public void restartAIServer() {

        LOG.info("Attempting to restart AI server");

        try {
            HttpHeaders headers = buildHeaders();
            LOG.debug("Headers built successfully for restart");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            LOG.debug("Calling AI server start API: {}", getStartApi());

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            getStartApi(),
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            LOG.debug("Restart API response code: {}", response.getStatusCode().value());
            LOG.debug("Restart API response body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                LOG.info("AI server restart triggered successfully");
                LOG.info("Waiting 20 seconds for AI server to stabilize");
                Thread.sleep(20_000);
            } else {
                LOG.warn("Failed to restart AI server, HTTP status: {}",
                         response.getStatusCode().value());
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new UnexpectedRunTimeException("AI server restart was interrupted", ie);
        } 
    }

    @Override
    public boolean waitUntilServerRunning() {

        LOG.info("Waiting for AI server to reach RUNNING state");

        while (true) {
            String status = getAIServerStatus();

            LOG.debug("Polled AI server status: {}", status);

            if ("running".equalsIgnoreCase(status)) {
                LOG.info("AI server is RUNNING");
                return true;
            }

            if ("stopped".equalsIgnoreCase(status)) {
                LOG.warn("AI server is STOPPED — initiating restart");
                restartAIServer();
            } else if (status.isEmpty()) {
                LOG.warn("AI server status unknown or empty");
            } else {
                LOG.debug("AI server in transitional state: {}", status);
            }

            try {
                LOG.debug("Sleeping 10 seconds before next status check");
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupted while waiting for AI server", e);
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}