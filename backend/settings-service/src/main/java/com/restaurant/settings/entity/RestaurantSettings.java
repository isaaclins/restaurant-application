package com.restaurant.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Restaurant Settings Entity - General restaurant configuration
 */
@Entity
@Table(name = "restaurant_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Restaurant Identity
    @Column(nullable = false)
    private String restaurantName;

    private String slogan;

    @Column(length = 1000)
    private String description;

    private String logoUrl;

    // Contact Information
    @Column(nullable = false)
    private String address;

    private String city;

    private String postalCode;

    private String country;

    private String phone;

    private String email;

    private String website;

    // Legal Information
    private String vatNumber;

    @Column(precision = 5, scale = 2)
    private BigDecimal vatRate;

    private String businessRegistration;

    // Order Configuration
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderValue = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal freeDeliveryThreshold;

    @Builder.Default
    private Integer estimatedDeliveryMinutes = 45;

    @Builder.Default
    private Integer estimatedPickupMinutes = 20;

    // Features
    @Builder.Default
    private Boolean deliveryEnabled = true;

    @Builder.Default
    private Boolean pickupEnabled = true;

    @Builder.Default
    private Boolean dineInEnabled = true;

    @Builder.Default
    private Boolean onlinePaymentEnabled = true;

    @Builder.Default
    private Boolean cashPaymentEnabled = true;

    @Builder.Default
    private Boolean cardPaymentEnabled = true;

    // Status
    @Builder.Default
    private Boolean isOpen = true;

    private String closedMessage;

    // Timestamps
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
