package com.restaurant.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Model - Stored in Redis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    private String sessionId;

    private List<CartItem> items;

    /**
     * Get items, initializing list if null (for deserialization safety)
     */
    public List<CartItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public int getTotalItems() {
        return getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal getTotalPrice() {
        return getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Add item to cart
     * If same product with same size and notes exists, merge quantities
     */
    public void addItem(CartItem item) {
        // Check if item with same configuration already exists
        for (CartItem existing : getItems()) {
            if (existing.matches(item)) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                return;
            }
        }
        getItems().add(item);
    }

    /**
     * Remove item by itemId (unique cart item identifier)
     */
    public void removeItemById(String itemId) {
        getItems().removeIf(item -> item.getItemId().equals(itemId));
    }

    /**
     * Remove all items with given productId
     */
    public void removeItem(Long productId) {
        getItems().removeIf(item -> item.getProductId().equals(productId));
    }

    /**
     * Update quantity for a specific cart item by itemId
     */
    public void updateItemQuantity(String itemId, int quantity) {
        for (CartItem item : getItems()) {
            if (item.getItemId().equals(itemId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    /**
     * Legacy: Update quantity by productId (updates first match)
     */
    public void updateItemQuantity(Long productId, int quantity) {
        for (CartItem item : getItems()) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    public void clear() {
        getItems().clear();
    }
}
