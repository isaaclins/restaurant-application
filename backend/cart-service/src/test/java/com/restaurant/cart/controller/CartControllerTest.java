package com.restaurant.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.cart.client.ProductClient;
import com.restaurant.cart.dto.AddToCartRequest;
import com.restaurant.cart.dto.ProductDTO;
import com.restaurant.cart.model.Cart;
import com.restaurant.cart.model.CartItem;
import com.restaurant.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@DisplayName("Cart Controller Tests")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private static final String SESSION_ID = "test-session-123";

    private Cart createTestCart() {
        Cart cart = new Cart();
        cart.setSessionId(SESSION_ID);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private CartItem createTestCartItem(Long productId, String name, BigDecimal price, int quantity) {
        return CartItem.builder()
                .productId(productId)
                .productName(name)
                .unitPrice(price)
                .quantity(quantity)
                .build();
    }

    @Nested
    @DisplayName("GET /api/cart")
    class GetCart {

        @Test
        @DisplayName("should return empty cart when no items")
        void shouldReturnEmptyCart() throws Exception {
            Cart emptyCart = createTestCart();
            when(cartService.getCart(SESSION_ID)).thenReturn(emptyCart);

            mockMvc.perform(get("/api/cart")
                    .header("X-Session-ID", SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.sessionId", is(SESSION_ID)))
                    .andExpect(jsonPath("$.items", hasSize(0)));

            verify(cartService).getCart(SESSION_ID);
        }

        @Test
        @DisplayName("should return cart with items")
        void shouldReturnCartWithItems() throws Exception {
            Cart cart = createTestCart();
            cart.getItems().add(createTestCartItem(1L, "Pizza", new BigDecimal("12.50"), 2));
            cart.getItems().add(createTestCartItem(2L, "Pasta", new BigDecimal("10.00"), 1));

            when(cartService.getCart(SESSION_ID)).thenReturn(cart);

            mockMvc.perform(get("/api/cart")
                    .header("X-Session-ID", SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].productName", is("Pizza")))
                    .andExpect(jsonPath("$.items[0].quantity", is(2)))
                    .andExpect(jsonPath("$.items[1].productName", is("Pasta")));
        }

        @Test
        @DisplayName("should return 400 when X-Session-ID header is missing")
        void shouldReturn400WhenSessionIdMissing() throws Exception {
            mockMvc.perform(get("/api/cart"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/cart/items")
    class AddItem {

        @Test
        @DisplayName("should add item to cart successfully")
        void shouldAddItemToCart() throws Exception {
            AddToCartRequest request = new AddToCartRequest(1L, 2);

            Cart updatedCart = createTestCart();
            updatedCart.getItems().add(createTestCartItem(1L, "Pizza", new BigDecimal("12.50"), 2));

            when(cartService.addItem(eq(SESSION_ID), any(AddToCartRequest.class))).thenReturn(updatedCart);

            mockMvc.perform(post("/api/cart/items")
                    .header("X-Session-ID", SESSION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].productId", is(1)))
                    .andExpect(jsonPath("$.items[0].quantity", is(2)));

            verify(cartService).addItem(eq(SESSION_ID), any(AddToCartRequest.class));
        }

        @Test
        @DisplayName("should return 400 when productId is null")
        void shouldReturn400WhenProductIdNull() throws Exception {
            AddToCartRequest request = new AddToCartRequest(null, 2);

            mockMvc.perform(post("/api/cart/items")
                    .header("X-Session-ID", SESSION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when quantity is less than 1")
        void shouldReturn400WhenQuantityInvalid() throws Exception {
            AddToCartRequest request = new AddToCartRequest(1L, 0);

            mockMvc.perform(post("/api/cart/items")
                    .header("X-Session-ID", SESSION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when X-Session-ID header is missing")
        void shouldReturn400WhenSessionIdMissing() throws Exception {
            AddToCartRequest request = new AddToCartRequest(1L, 2);

            mockMvc.perform(post("/api/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/cart/items/{productId}")
    class RemoveItem {

        @Test
        @DisplayName("should remove item from cart successfully")
        void shouldRemoveItemFromCart() throws Exception {
            Cart updatedCart = createTestCart();

            when(cartService.removeItem(SESSION_ID, 1L)).thenReturn(updatedCart);

            mockMvc.perform(delete("/api/cart/items/{productId}", 1L)
                    .header("X-Session-ID", SESSION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)));

            verify(cartService).removeItem(SESSION_ID, 1L);
        }

        @Test
        @DisplayName("should return 400 when X-Session-ID header is missing")
        void shouldReturn400WhenSessionIdMissing() throws Exception {
            mockMvc.perform(delete("/api/cart/items/{productId}", 1L))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/cart")
    class ClearCart {

        @Test
        @DisplayName("should clear cart successfully")
        void shouldClearCart() throws Exception {
            doNothing().when(cartService).clearCart(SESSION_ID);

            mockMvc.perform(delete("/api/cart")
                    .header("X-Session-ID", SESSION_ID))
                    .andExpect(status().isNoContent());

            verify(cartService).clearCart(SESSION_ID);
        }

        @Test
        @DisplayName("should return 400 when X-Session-ID header is missing")
        void shouldReturn400WhenSessionIdMissing() throws Exception {
            mockMvc.perform(delete("/api/cart"))
                    .andExpect(status().isBadRequest());
        }
    }
}
