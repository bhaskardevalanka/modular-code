package com.techvedika.harmonycvi.gateway.serviceimpl;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.messaging.QueueProducer;

@Service
public class AIQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(AIQueueService.class);
    
    private QueueProducer queueProducer;
    
    public AIQueueService(QueueProducer queueProducer) {
    	this.queueProducer = queueProducer;
    }

    @Async
    public void enqueueAIRequest(String aiUrl, JSONObject request2) {
        LOG.info("Running async enqueueAIRequest for URL: {}", aiUrl);
        JSONObject request = new JSONObject();
        request.put("url", aiUrl);
        request.put("method", "POST");
        request.put("isUpload", true);
        request.put("request", request2);
        try {
			queueProducer.addToQueue(request);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ERROR while adding request to Queue:{}",e.getLocalizedMessage());
		}
        LOG.info("Finished async enqueue for URL: {}", aiUrl);
    }
}