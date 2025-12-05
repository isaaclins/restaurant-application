package com.restaurant.receipt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create Receipt Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReceiptRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Order number is required")
    private String orderNumber;

    // Customer Info
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Long customerId;

    // Order Details
    private String orderType;
    private String paymentMethod;

    // Pricing
    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    private BigDecimal deliveryFee;
    private BigDecimal discount;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    // Items
    @NotNull(message = "Items are required")
    @Valid
    private List<CreateReceiptItemRequest> items;
}
