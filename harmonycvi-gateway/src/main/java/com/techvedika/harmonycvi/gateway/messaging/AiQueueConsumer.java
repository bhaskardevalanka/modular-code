package com.techvedika.harmonycvi.gateway.messaging;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.cloud.AbstractAiServerController;
import com.techvedika.harmonycvi.gateway.exception.UnexpectedRunTimeException;
import com.techvedika.harmonycvi.gateway.serviceimpl.AiRedisCacheService;

@Service
@ConditionalOnProperty(name = "messaging.activemq.enabled", havingValue = "true")
public class AiQueueConsumer {

    private static final Logger LOG =
            LoggerFactory.getLogger(AiQueueConsumer.class);

    private final AbstractAiServerController aiServerController;
    private final AiRedisCacheService aiRedisCacheService;
    private final JmsTemplate jmsTemplate;

    public AiQueueConsumer(AbstractAiServerController controller,
                           AiRedisCacheService aiRedisCacheService,
                           JmsTemplate jmsTemplate) {
        this.aiServerController = controller;
        this.aiRedisCacheService = aiRedisCacheService;
        this.jmsTemplate = jmsTemplate;
    }


    @JmsListener(
            destination = "${messaging.queue-name}",
            containerFactory = "jmsListenerContainerFactory"
    )
    public void onMessage(String message) {

        LOG.info("🔥 JMS message received");

        try {
        	// 🔥 Push activity timestamp
            pushTimestamp();
            
            JSONObject resObj =
                    (JSONObject) new JSONParser().parse(message);

            aiServerController.waitUntilServerRunning();

            while (!aiRedisCacheService.isReady()) {
                LOG.info("AI service not ready, waiting...");
                Thread.sleep(10_000);
            }

            if (!"alert".equalsIgnoreCase(resObj.get("url").toString())) {
                aiRedisCacheService.processRequest(resObj);
            }

            LOG.info("✅ JMS message processed successfully");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new UnexpectedRunTimeException("JMS message processing was interrupted", ie);
        } catch (Exception e) {
            LOG.error("❌ Error processing JMS message", e);
            throw new UnexpectedRunTimeException(e.getMessage(), e);
        }
    }
    
    private void pushTimestamp() {
        long now = System.currentTimeMillis();
        LOG.info("Timestamp:{}",now);
        jmsTemplate.convertAndSend(
                "request_timestamps_queue",
                String.valueOf(now)
        );
        LOG.info("Pushed activity timestamp: {}", now);
    }
}
