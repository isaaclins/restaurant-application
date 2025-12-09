package com.restaurant.settings.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Create/Update Delivery Area Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAreaRequest {

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @JsonProperty("city")
    @JsonAlias("areaName") // backward compatibility with older clients
    private String city;

    @PositiveOrZero(message = "Delivery fee must be zero or positive")
    private BigDecimal deliveryFee;

    @PositiveOrZero(message = "Minimum order value must be zero or positive")
    private BigDecimal minimumOrderValue;

    private Integer estimatedDeliveryMinutes;

    private Boolean isActive;
}
