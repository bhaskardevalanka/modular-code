package com.techvedika.harmonycvi.gateway.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.techvedika.harmonycvi.gateway.cloud.AiServerController;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
@EnableScheduling
public class CustomScheduler {

    private static final Logger LOG =
            LoggerFactory.getLogger(CustomScheduler.class);

    @Value("${messaging.activemq.enabled:false}")
    private String activeMqSwitch;

    @Value("${ai.max-idle-time:90000}")
    private long maxAiIdleTime;

    private final AiServerController controller;
    private final JmsTemplate jmsTemplate;

    private static final String TIMESTAMP_QUEUE =
            "request_timestamps_queue";

    public CustomScheduler(AiServerController controller,
                           JmsTemplate jmsTemplate) {
        this.controller = controller;
        this.jmsTemplate = jmsTemplate;
    }

    @PostConstruct
    public void init() {
        LOG.info("CustomScheduler initialized");
    }

    @PreDestroy
    public void shutdown() {
        LOG.info("CustomScheduler shutting down");
    }

    /**
     * Periodic scheduler
     */
    @Scheduled(fixedRateString = "${STATUS_CHECK_SCHEDULER_RATE:90000}")
    public void execute() {

        LOG.info("CustomScheduler triggered at {}", new Date());

        if (!"true".equalsIgnoreCase(activeMqSwitch)) {
            LOG.info("ActiveMQ switch is OFF, skipping execution");
            return;
        }

        try {
            List<Long> timestamps = drainTimestampQueue();

            if (timestamps.isEmpty()) {
                LOG.info("No timestamps found, skipping AI idle check");
                return;
            }

            long now = System.currentTimeMillis();
            long latestTs = 0L;
            int expiredCount = 0;

            for (Long ts : timestamps) {
                if (now - ts > maxAiIdleTime) {
                    expiredCount++;
                }
                latestTs = Math.max(latestTs, ts);
            }

            if (expiredCount == timestamps.size()) {
                LOG.info("AI server idle detected, invoking stop logic");
                controller.handleIdleServer();
            } else {
                LOG.info("AI server is active, skipping stop");
            }

            // Push latest timestamp back
            jmsTemplate.convertAndSend(
                    TIMESTAMP_QUEUE,
                    String.valueOf(latestTs)
            );

            LOG.info("Re-queued latest timestamp: {}", latestTs);

        } catch (Exception e) {
            LOG.error("Exception during scheduler execution", e);
        }
    }

    /**
     * Drain all messages from timestamp queue
     */
    private List<Long> drainTimestampQueue() {

        List<Long> timestamps = new ArrayList<>();

        while (true) {
            String msg =
                    (String) jmsTemplate.receiveAndConvert(
                            TIMESTAMP_QUEUE
                    );

            if (msg == null) {
                break;
            }

            LOG.info("Received timestamp from queue: {}", msg);
            timestamps.add(Long.parseLong(msg));
        }

        return timestamps;
    }
}
