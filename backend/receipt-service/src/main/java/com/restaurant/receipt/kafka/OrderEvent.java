package com.restaurant.receipt.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order event received from Order Service via Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;
    private String orderNumber;
    private String status;

    // Customer info
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Long customerId;

    // Order details
    private String orderType;
    private String paymentMethod;
    private String notes;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal totalPrice;

    // Items
    private List<OrderItemEvent> items;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String notes;
    }
}
