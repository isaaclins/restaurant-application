package com.restaurant.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Delivery Area Entity - Defines areas where delivery is available
 */
@Entity
@Table(name = "delivery_areas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String postalCode;

    private String areaName;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal minimumOrderValue;

    private Integer estimatedDeliveryMinutes;

    @Builder.Default
    private Boolean isActive = true;
}
