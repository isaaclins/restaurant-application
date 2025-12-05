package com.restaurant.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer Address Entity
 */
@Entity
@Table(name = "customer_addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String street;

    private String streetNumber;

    private String additionalInfo; // Apartment, floor, etc.

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String city;

    @Builder.Default
    private String country = "Schweiz";

    private String label; // "Home", "Work", etc.

    @Builder.Default
    private Boolean isDefault = false;

    // Delivery notes
    private String deliveryInstructions;
}
