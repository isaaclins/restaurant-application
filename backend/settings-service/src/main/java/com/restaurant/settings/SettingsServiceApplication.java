package com.restaurant.settings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Settings Service Application
 * 
 * Manages restaurant configuration: opening hours, delivery areas,
 * minimum order values, payment methods, and general settings.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SettingsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettingsServiceApplication.class, args);
    }
}
