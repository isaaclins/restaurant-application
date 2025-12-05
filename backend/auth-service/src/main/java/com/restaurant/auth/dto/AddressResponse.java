package com.restaurant.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String street;
    private String streetNumber;
    private String additionalInfo;
    private String postalCode;
    private String city;
    private String country;
    private String label;
    private Boolean isDefault;
    private String deliveryInstructions;

    // Formatted full address
    private String fullAddress;
}
