package com.restaurant.receipt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Restaurant Configuration Properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "restaurant")
public class RestaurantConfig {

    private String name = "Restaurant App";
    private String address = "Musterstrasse 1, 8000 Zürich";
    private String phone = "+41 44 123 45 67";
    private String vatNumber = "CHE-123.456.789";
    private BigDecimal vatRate = new BigDecimal("7.7");
}
