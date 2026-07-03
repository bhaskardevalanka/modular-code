package com.techvedika.harmonycvi.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.util.ApiException;

@RestController
@RequestMapping("/hello")
public class TestHelloWorldController {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestHelloWorldController.class);
	
	@GetMapping(value = "/test")
    public ResponseEntity<?> test() {
        try {
            LOG.info("Service hello test");
            return ResponseEntity.ok("Hello there, Server is up");
        } catch (ApiException ae) {
            // Custom exception you define and throw from userUtils.globalException
            return ResponseEntity
                    .status(ae.getStatus())
                    .body(ae.getBody());
        }
    }
}
