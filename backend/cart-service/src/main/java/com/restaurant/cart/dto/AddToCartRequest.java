package com.restaurant.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding items to cart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    /**
     * Product size (e.g., "S", "M", "L" for pizza)
     * Optional - null means default/standard size
     */
    @Size(max = 10, message = "Size must be at most 10 characters")
    private String size;

    /**
     * Special notes/instructions for this item
     * Optional - customer requests like "extra crispy", "no onions"
     */
    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;
}
