package com.restaurant.cart.service;

import com.restaurant.cart.client.ProductClient;
import com.restaurant.cart.dto.AddToCartRequest;
import com.restaurant.cart.dto.ProductDTO;
import com.restaurant.cart.model.Cart;
import com.restaurant.cart.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Cart Service - Business Logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Cart> redisTemplate;
    private final ProductClient productClient;

    private static final String CART_PREFIX = "cart:";
    private static final long CART_TTL_HOURS = 24;

    /**
     * Get cart by session ID
     */
    public Cart getCart(String sessionId) {
        String key = CART_PREFIX + sessionId;
        Cart cart = redisTemplate.opsForValue().get(key);

        if (cart == null) {
            cart = Cart.builder()
                    .sessionId(sessionId)
                    .build();
        }

        return cart;
    }

    /**
     * Add item to cart
     */
    public Cart addItem(String sessionId, AddToCartRequest request) {
        // Validate product exists
        ProductDTO product = productClient.getProductById(request.getProductId());

        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + request.getProductId());
        }

        if (!product.getAvailable()) {
            throw new IllegalArgumentException("Product is not available: " + product.getName());
        }

        Cart cart = getCart(sessionId);

        CartItem item = CartItem.builder()
                .itemId(java.util.UUID.randomUUID().toString())
                .productId(product.getId())
                .productName(product.getName())
                .unitPrice(product.getPrice())
                .quantity(request.getQuantity())
                .size(request.getSize())
                .notes(request.getNotes())
                .build();

        cart.addItem(item);
        saveCart(cart);

        String sizeInfo = request.getSize() != null ? " (" + request.getSize() + ")" : "";
        log.info("Added {} x {}{} to cart {}", request.getQuantity(), product.getName(), sizeInfo, sessionId);

        return cart;
    }

    /**
     * Remove item from cart by itemId
     */
    public Cart removeItemById(String sessionId, String itemId) {
        Cart cart = getCart(sessionId);
        cart.removeItemById(itemId);
        saveCart(cart);

        log.info("Removed item {} from cart {}", itemId, sessionId);

        return cart;
    }

    /**
     * Remove item from cart by productId (legacy, removes all with that productId)
     */
    public Cart removeItem(String sessionId, Long productId) {
        Cart cart = getCart(sessionId);
        cart.removeItem(productId);
        saveCart(cart);

        log.info("Removed product {} from cart {}", productId, sessionId);

        return cart;
    }

    /**
     * Update item quantity by itemId
     */
    public Cart updateItemQuantityById(String sessionId, String itemId, int quantity) {
        if (quantity <= 0) {
            return removeItemById(sessionId, itemId);
        }

        Cart cart = getCart(sessionId);
        cart.updateItemQuantity(itemId, quantity);
        saveCart(cart);

        log.info("Updated quantity for item {} in cart {}: {}", itemId, sessionId, quantity);

        return cart;
    }

    /**
     * Update item quantity by productId (legacy)
     */
    public Cart updateItemQuantity(String sessionId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeItem(sessionId, productId);
        }

        Cart cart = getCart(sessionId);
        cart.updateItemQuantity(productId, quantity);
        saveCart(cart);

        log.info("Updated quantity for product {} in cart {}: {}", productId, sessionId, quantity);

        return cart;
    }

    /**
     * Clear cart
     */
    public void clearCart(String sessionId) {
        String key = CART_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.info("Cleared cart: {}", sessionId);
    }

    /**
     * Save cart to Redis
     */
    private void saveCart(Cart cart) {
        String key = CART_PREFIX + cart.getSessionId();
        redisTemplate.opsForValue().set(key, cart, CART_TTL_HOURS, TimeUnit.HOURS);
    }
}
