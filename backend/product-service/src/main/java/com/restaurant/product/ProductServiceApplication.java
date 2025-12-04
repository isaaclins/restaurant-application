package com.restaurant.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Product Service - Product Catalog Management
 * 
 * Manages products, categories, and availability.
 * Uses MySQL for persistent storage.
 * 
 * Port: 8081
 * Swagger: http://localhost:8081/swagger-ui.html
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
