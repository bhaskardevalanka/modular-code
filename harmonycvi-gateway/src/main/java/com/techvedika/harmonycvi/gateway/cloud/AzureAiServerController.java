package com.techvedika.harmonycvi.gateway.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.serviceimpl.AiRedisCacheService;

@Component
@ConditionalOnProperty(name = "cloud.provider", havingValue = "azure")
public class AzureAiServerController extends AbstractAiServerController {

    @Value("${azure.functions.base-url}")
    private String azureBaseUrl;

    @Value("${azure.functions.api-key}")
    private String apiKey;

    public AzureAiServerController(RestTemplate restTemplate,
                                 ObjectMapper objectMapper,
                                 AiRedisCacheService redisCacheService) {
        super(restTemplate, objectMapper, redisCacheService);
    }

    @Override
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // Azure Functions typically use x-functions-key for authentication
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("x-functions-key", apiKey);
        }
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Override
    protected String getCheckStatusApi() {
        return azureBaseUrl + "?action=status";
    }

    @Override
    protected String getStopApi() {
        return azureBaseUrl + "?action=stop";
    }

    @Override
    protected String getStartApi() {
        return azureBaseUrl + "?action=start";
    }
}
