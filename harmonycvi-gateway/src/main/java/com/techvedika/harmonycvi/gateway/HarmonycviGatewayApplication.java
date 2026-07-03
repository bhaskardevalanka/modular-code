package com.techvedika.harmonycvi.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.techvedika.harmonycvi")
@EntityScan(basePackages = "com.techvedika.harmonycvi.gateway.entity")
@EnableJpaRepositories(basePackages = "com.techvedika.harmonycvi.gateway.repository")
@EnableAsync
public class HarmonycviGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(HarmonycviGatewayApplication.class, args);
	}
}
