package com.restaurant.receipt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Receipt Service Application
 * 
 * Handles receipt generation, PDF creation, and daily reporting.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ReceiptServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptServiceApplication.class, args);
    }
}
