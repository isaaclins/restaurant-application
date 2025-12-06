package com.restaurant.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Cart Item Model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    /**
     * Unique identifier for this cart item
     * Different from productId - allows same product with different options
     */
    private String itemId;

    /**
     * Generate itemId if not set (for deserialization compatibility)
     */
    public String getItemId() {
        if (itemId == null) {
            itemId = UUID.randomUUID().toString();
        }
        return itemId;
    }

    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;

    /**
     * Product size (e.g., "S", "M", "L")
     */
    private String size;

    /**
     * Special notes/instructions
     */
    private String notes;

    public BigDecimal getTotalPrice() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Check if this item matches another item (same product + size + notes)
     * Used to merge quantities when adding same configuration
     */
    public boolean matches(CartItem other) {
        return Objects.equals(this.productId, other.productId)
                && Objects.equals(this.size, other.size)
                && Objects.equals(this.notes, other.notes);
    }
}
