package com.restaurant.cart.controller;

import com.restaurant.cart.dto.AddToCartRequest;
import com.restaurant.cart.model.Cart;
import com.restaurant.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart Controller - REST API Endpoints
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart - Get current cart
     */
    @GetMapping
    @Operation(summary = "Get cart", description = "Get current shopping cart")
    public ResponseEntity<Cart> getCart(
            @RequestHeader("X-Session-ID") String sessionId) {

        Cart cart = cartService.getCart(sessionId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/items - Add item to cart
     */
    @PostMapping("/items")
    @Operation(summary = "Add item", description = "Add item to shopping cart")
    public ResponseEntity<Cart> addItem(
            @RequestHeader("X-Session-ID") String sessionId,
            @Valid @RequestBody AddToCartRequest request) {

        Cart cart = cartService.addItem(sessionId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/items/{productId} - Remove item from cart
     */
    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item", description = "Remove item from shopping cart")
    public ResponseEntity<Cart> removeItem(
            @RequestHeader("X-Session-ID") String sessionId,
            @PathVariable Long productId) {

        Cart cart = cartService.removeItem(sessionId, productId);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart - Clear cart
     */
    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from cart")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-Session-ID") String sessionId) {

        cartService.clearCart(sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/cart/health - Health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "service", "cart-service"));
    }
}
