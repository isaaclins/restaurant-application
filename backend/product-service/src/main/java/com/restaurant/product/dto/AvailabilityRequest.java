package com.restaurant.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for availability updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequest {

    @NotNull(message = "Available status is required")
    private Boolean available;
}
