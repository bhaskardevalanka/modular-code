package com.techvedika.harmonycvi.gateway.cloud;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.serviceimpl.AiRedisCacheService;

@Component
@ConditionalOnProperty(name = "cloud.provider", havingValue = "gcp")
public class GcpAiServerController extends AbstractAiServerController {

    private static final Logger LOG =
            LoggerFactory.getLogger(GcpAiServerController.class);

    @Value("${gcp.cloudrun.base-url}")
    private String gcpBaseUrl;

    public GcpAiServerController(RestTemplate restTemplate,
                                 ObjectMapper objectMapper,
                                 AiRedisCacheService redisCacheService) {
        super(restTemplate, objectMapper, redisCacheService);

        LOG.info("Initialized GCP AI Server Controller");
    }

    @Override
    protected HttpHeaders buildHeaders() {
            LOG.info("Generating GCP ID token for Cloud Run audience: {}", gcpBaseUrl);

            String token = "";
			try {
				token = GcpAuthUtil.getIdToken(gcpBaseUrl);
			} catch (IOException e) {
				e.printStackTrace();
			}

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            LOG.info("Successfully generated GCP ID token");

            return headers;
    }

    @Override
    protected String getCheckStatusApi() {
        return gcpBaseUrl + "?action=status";
    }

    @Override
    protected String getStopApi() {
        return gcpBaseUrl + "?action=stop";
    }

    @Override
    protected String getStartApi() {
        return gcpBaseUrl + "?action=start";
    }
}
