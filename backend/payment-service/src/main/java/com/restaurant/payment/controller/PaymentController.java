package com.restaurant.payment.controller;

import com.restaurant.payment.dto.PaymentRequest;
import com.restaurant.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Controller - Mockup Implementation
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing (Mockup)")
@Slf4j
public class PaymentController {

    @Value("${payment.test.decline-card:4000000000000002}")
    private String declineTestCard;

    /**
     * POST /api/payments - Process payment
     * 
     * This is a MOCKUP - always returns success except for test decline card
     */
    @PostMapping
    @Operation(summary = "Process payment", description = "Process a payment (Mockup - always succeeds)")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {

        log.info("Processing payment for order: {}", request.getOrderId());

        // Simulate failure for test card (configurable)
        if (declineTestCard.equals(request.getCardNumber())) {
            return ResponseEntity.status(402).body(
                    PaymentResponse.builder()
                            .paymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                            .status("FAILED")
                            .message("Card declined")
                            .processedAt(LocalDateTime.now())
                            .build());
        }

        // Simulate successful payment
        PaymentResponse response = PaymentResponse.builder()
                .paymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .status("COMPLETED")
                .message("Payment successful")
                .amount(request.getAmount())
                .processedAt(LocalDateTime.now())
                .build();

        log.info("Payment successful: {}", response.getPaymentId());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payments/{id} - Get payment status
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get payment status")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String id) {
        // Mockup - return static response
        return ResponseEntity.ok(
                PaymentResponse.builder()
                        .paymentId(id)
                        .status("COMPLETED")
                        .message("Payment found")
                        .processedAt(LocalDateTime.now())
                        .build());
    }
}
