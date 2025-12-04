package com.restaurant.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway - Central Entry Point
 * 
 * Routes all requests to appropriate microservices.
 * Handles CORS, rate limiting, and circuit breaking.
 * 
 * URL: http://localhost:8080
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
