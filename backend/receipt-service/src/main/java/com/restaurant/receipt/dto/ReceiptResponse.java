package com.restaurant.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Receipt Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private Long id;
    private String receiptNumber;
    private Long orderId;
    private String orderNumber;

    // Customer
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Restaurant
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    private String vatNumber;

    // Order Details
    private String orderType;
    private String paymentMethod;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal vatRate;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal totalAmount;

    // Items
    private List<ReceiptItemResponse> items;

    // Timestamps
    private LocalDateTime createdAt;

    // PDF available
    private boolean pdfAvailable;
}
