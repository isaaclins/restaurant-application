package com.restaurant.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Restaurant Settings Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSettingsResponse {

    private Long id;

    // Identity
    private String restaurantName;
    private String slogan;
    private String description;
    private String logoUrl;

    // Contact
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String phone;
    private String email;
    private String website;

    // Legal
    private String vatNumber;
    private BigDecimal vatRate;
    private String businessRegistration;

    // Order Configuration
    private BigDecimal minimumOrderValue;
    private BigDecimal deliveryFee;
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
    private Boolean isCurrentlyOpen; // Computed based on opening hours

    // Opening Hours
    private List<OpeningHoursResponse> openingHours;

    // Delivery Areas
    private List<DeliveryAreaResponse> deliveryAreas;

    private LocalDateTime updatedAt;
}
