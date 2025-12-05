package com.restaurant.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Update Restaurant Settings Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingsRequest {

    @NotBlank(message = "Restaurant name is required")
    private String restaurantName;

    private String slogan;
    private String description;
    private String logoUrl;

    // Contact
    @NotBlank(message = "Address is required")
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String phone;
    private String email;
    private String website;

    // Legal
    private String vatNumber;

    @PositiveOrZero(message = "VAT rate must be zero or positive")
    private BigDecimal vatRate;

    private String businessRegistration;

    // Order Configuration
    @PositiveOrZero(message = "Minimum order value must be zero or positive")
    private BigDecimal minimumOrderValue;

    @PositiveOrZero(message = "Delivery fee must be zero or positive")
    private BigDecimal deliveryFee;

    @PositiveOrZero(message = "Free delivery threshold must be zero or positive")
    private BigDecimal freeDeliveryThreshold;

    private Integer estimatedDeliveryMinutes;
    private Integer estimatedPickupMinutes;

    // Features
    private Boolean deliveryEnabled;
    private Boolean pickupEnabled;
    private Boolean dineInEnabled;
    private Boolean onlinePaymentEnabled;
    private Boolean cashPaymentEnabled;
    private Boolean cardPaymentEnabled;

    // Status
    private Boolean isOpen;
    private String closedMessage;
}
