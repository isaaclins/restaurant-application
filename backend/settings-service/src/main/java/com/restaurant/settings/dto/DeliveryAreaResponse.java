package com.restaurant.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Delivery Area Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAreaResponse {
    private Long id;
    private String postalCode;
    private String areaName;
    private BigDecimal deliveryFee;
    private BigDecimal minimumOrderValue;
    private Integer estimatedDeliveryMinutes;
    private Boolean isActive;
}
