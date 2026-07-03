package com.techvedika.harmonycvi.gateway.messaging;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueProducer {

    private static final Logger LOG =
            LoggerFactory.getLogger(QueueProducer.class);

    private final JmsTemplate jmsTemplate;

    @Value("${messaging.queue-name}")
    private String queueName;

    public QueueProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void addToQueue(JSONObject request) {

        LOG.info("Sending message to queue {}: {}", queueName, request);

        jmsTemplate.convertAndSend(queueName, request.toJSONString());
    }
}