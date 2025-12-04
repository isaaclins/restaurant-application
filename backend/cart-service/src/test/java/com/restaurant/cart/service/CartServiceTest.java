package com.restaurant.cart.service;

import com.restaurant.cart.client.ProductClient;
import com.restaurant.cart.dto.AddToCartRequest;
import com.restaurant.cart.dto.ProductDTO;
import com.restaurant.cart.model.Cart;
import com.restaurant.cart.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Unit Tests")
class CartServiceTest {

    @Mock
    private RedisTemplate<String, Cart> redisTemplate;

    @Mock
    private ValueOperations<String, Cart> valueOperations;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    private static final String SESSION_ID = "test-session-123";
    private static final String CART_KEY = "cart:" + SESSION_ID;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private ProductDTO createTestProduct(Long id, String name, BigDecimal price, boolean available) {
        return ProductDTO.builder()
                .id(id)
                .name(name)
                .description("Test " + name)
                .price(price)
                .category("Pizza")
                .available(available)
                .build();
    }

    private Cart createTestCart() {
        Cart cart = new Cart();
        cart.setSessionId(SESSION_ID);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("should return existing cart from Redis")
        void shouldReturnExistingCart() {
            Cart existingCart = createTestCart();
            existingCart.getItems().add(CartItem.builder()
                    .productId(1L)
                    .productName("Pizza")
                    .unitPrice(new BigDecimal("12.50"))
                    .quantity(2)
                    .build());

            when(valueOperations.get(CART_KEY)).thenReturn(existingCart);

            Cart result = cartService.getCart(SESSION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
            assertThat(result.getItems()).hasSize(1);
            verify(valueOperations).get(CART_KEY);
        }

        @Test
        @DisplayName("should return new cart when not found in Redis")
        void shouldReturnNewCartWhenNotFound() {
            when(valueOperations.get(CART_KEY)).thenReturn(null);

            Cart result = cartService.getCart(SESSION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
            assertThat(result.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("should add new item to empty cart")
        void shouldAddNewItemToEmptyCart() {
            ProductDTO product = createTestProduct(1L, "Margherita", new BigDecimal("12.50"), true);
            AddToCartRequest request = new AddToCartRequest(1L, 2);

            when(productClient.getProductById(1L)).thenReturn(product);
            when(valueOperations.get(CART_KEY)).thenReturn(null);

            Cart result = cartService.addItem(SESSION_ID, request);

            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getProductId()).isEqualTo(1L);
            assertThat(result.getItems().get(0).getProductName()).isEqualTo("Margherita");
            assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
            assertThat(result.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("12.50"));

            verify(valueOperations).set(eq(CART_KEY), any(Cart.class), anyLong(), any());
        }

        @Test
        @DisplayName("should increase quantity when adding existing item")
        void shouldIncreaseQuantityForExistingItem() {
            ProductDTO product = createTestProduct(1L, "Margherita", new BigDecimal("12.50"), true);
            AddToCartRequest request = new AddToCartRequest(1L, 3);

            Cart existingCart = createTestCart();
            existingCart.getItems().add(CartItem.builder()
                    .productId(1L)
                    .productName("Margherita")
                    .unitPrice(new BigDecimal("12.50"))
                    .quantity(2)
                    .build());

            when(productClient.getProductById(1L)).thenReturn(product);
            when(valueOperations.get(CART_KEY)).thenReturn(existingCart);

            Cart result = cartService.addItem(SESSION_ID, request);

            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5); // 2 + 3
        }

        @Test
        @DisplayName("should throw exception when product not found")
        void shouldThrowWhenProductNotFound() {
            AddToCartRequest request = new AddToCartRequest(999L, 1);
            when(productClient.getProductById(999L)).thenReturn(null);

            assertThatThrownBy(() -> cartService.addItem(SESSION_ID, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("should throw exception when product not available")
        void shouldThrowWhenProductNotAvailable() {
            ProductDTO product = createTestProduct(1L, "Seasonal Special", new BigDecimal("20.00"), false);
            AddToCartRequest request = new AddToCartRequest(1L, 1);

            when(productClient.getProductById(1L)).thenReturn(product);

            assertThatThrownBy(() -> cartService.addItem(SESSION_ID, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not available");
        }
    }

    @Nested
    @DisplayName("removeItem")
    class RemoveItem {

        @Test
        @DisplayName("should remove item from cart")
        void shouldRemoveItemFromCart() {
            Cart existingCart = createTestCart();
            existingCart.getItems().add(CartItem.builder()
                    .productId(1L)
                    .productName("Pizza")
                    .unitPrice(new BigDecimal("12.50"))
                    .quantity(2)
                    .build());
            existingCart.getItems().add(CartItem.builder()
                    .productId(2L)
                    .productName("Pasta")
                    .unitPrice(new BigDecimal("10.00"))
                    .quantity(1)
                    .build());

            when(valueOperations.get(CART_KEY)).thenReturn(existingCart);

            Cart result = cartService.removeItem(SESSION_ID, 1L);

            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getProductId()).isEqualTo(2L);
            verify(valueOperations).set(eq(CART_KEY), any(Cart.class), anyLong(), any());
        }

        @Test
        @DisplayName("should handle removing non-existent item gracefully")
        void shouldHandleNonExistentItem() {
            Cart existingCart = createTestCart();
            existingCart.getItems().add(CartItem.builder()
                    .productId(1L)
                    .productName("Pizza")
                    .unitPrice(new BigDecimal("12.50"))
                    .quantity(2)
                    .build());

            when(valueOperations.get(CART_KEY)).thenReturn(existingCart);

            Cart result = cartService.removeItem(SESSION_ID, 999L);

            assertThat(result.getItems()).hasSize(1); // Original item still there
        }
    }

    @Nested
    @DisplayName("clearCart")
    class ClearCart {

        @Test
        @DisplayName("should delete cart from Redis")
        void shouldDeleteCartFromRedis() {
            when(redisTemplate.delete(CART_KEY)).thenReturn(true);

            cartService.clearCart(SESSION_ID);

            verify(redisTemplate).delete(CART_KEY);
        }
    }
}
