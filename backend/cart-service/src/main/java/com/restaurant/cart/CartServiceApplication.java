package com.restaurant.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Cart Service - Shopping Cart Management
 * 
 * Uses Redis for session-based cart storage.
 * Communicates with Product Service for validation.
 * 
 * Port: 8082
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
