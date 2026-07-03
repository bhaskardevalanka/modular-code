package com.techvedika.harmonycvi.gateway.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.serviceimpl.AiRedisCacheService;

@Component
@ConditionalOnProperty(name = "cloud.provider", havingValue = "aws")
public class AwsAiServerController extends AbstractAiServerController {

    @Value("${aws.lambda.base-url}")
    private String lambdaBaseUrl;

    @Value("${aws.lambda.x-api-key}")
    private String xApiKey;

    public AwsAiServerController(RestTemplate restTemplate,
                                 ObjectMapper objectMapper,
                                 AiRedisCacheService redisCacheService) {
        super(restTemplate, objectMapper,redisCacheService);
    }

    @Override
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", xApiKey);
        return headers;
    }

    @Override
    protected String getCheckStatusApi() {
        return lambdaBaseUrl + "/statusInstance";
    }

    @Override
    protected String getStopApi() {
        return lambdaBaseUrl + "/stopInstance";
    }

    @Override
    protected String getStartApi() {
        return lambdaBaseUrl + "/startInstance";
    }
}
