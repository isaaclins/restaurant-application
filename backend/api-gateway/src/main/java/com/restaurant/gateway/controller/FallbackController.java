package com.restaurant.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker
 * 
 * Returns graceful error responses when services are unavailable.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> productsFallback() {
        return createFallbackResponse("Product Service", "Products are temporarily unavailable");
    }

    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> cartFallback() {
        return createFallbackResponse("Cart Service", "Cart is temporarily unavailable");
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> ordersFallback() {
        return createFallbackResponse("Order Service", "Orders are temporarily unavailable");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String service, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("service", service);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("error", "SERVICE_UNAVAILABLE");
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response);
    }
}
