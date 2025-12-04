package com.restaurant.gateway.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.*;

@WebFluxTest(FallbackController.class)
@ActiveProfiles("test")
@DisplayName("Fallback Controller Tests")
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Nested
    @DisplayName("GET /fallback/products")
    class ProductsFallback {

        @Test
        @DisplayName("should return service unavailable for products")
        void shouldReturnServiceUnavailable() {
            webTestClient.get()
                    .uri("/fallback/products")
                    .exchange()
                    .expectStatus().isEqualTo(503)
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false)
                    .jsonPath("$.service").isEqualTo("Product Service")
                    .jsonPath("$.message").isEqualTo("Products are temporarily unavailable")
                    .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
                    .jsonPath("$.timestamp").exists();
        }
    }

    @Nested
    @DisplayName("GET /fallback/cart")
    class CartFallback {

        @Test
        @DisplayName("should return service unavailable for cart")
        void shouldReturnServiceUnavailable() {
            webTestClient.get()
                    .uri("/fallback/cart")
                    .exchange()
                    .expectStatus().isEqualTo(503)
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false)
                    .jsonPath("$.service").isEqualTo("Cart Service")
                    .jsonPath("$.message").isEqualTo("Cart is temporarily unavailable")
                    .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE");
        }
    }

    @Nested
    @DisplayName("GET /fallback/orders")
    class OrdersFallback {

        @Test
        @DisplayName("should return service unavailable for orders")
        void shouldReturnServiceUnavailable() {
            webTestClient.get()
                    .uri("/fallback/orders")
                    .exchange()
                    .expectStatus().isEqualTo(503)
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false)
                    .jsonPath("$.service").isEqualTo("Order Service")
                    .jsonPath("$.message").isEqualTo("Orders are temporarily unavailable")
                    .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE");
        }
    }
}
