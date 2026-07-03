package com.techvedika.harmonycvi.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginConcurrencyTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String LOGIN_URL = "/users/login";

    @Test
    void testConcurrentLoginUpdates() throws Exception {
        // Request payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "sruthicvi@yopmail.com");
        requestBody.put("isAdmin", "no");
        requestBody.put("password", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Run two requests in parallel
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<ResponseEntity<String>> task = () -> restTemplate.postForEntity(LOGIN_URL, request, String.class);

        Future<ResponseEntity<String>> future1 = executor.submit(task);
        Future<ResponseEntity<String>> future2 = executor.submit(task);

        ResponseEntity<String> response1 = future1.get();
        ResponseEntity<String> response2 = future2.get();

        executor.shutdown();

        // Print both responses
        System.out.println("Response 1: " + response1);
        System.out.println("Response 2: " + response2);

        // Assert both responses are OK, but DB version should only allow one update
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
