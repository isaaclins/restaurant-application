package com.restaurant.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for order events received via Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private Long orderId;
    private String orderNumber;
    private String status;
    private String previousStatus;
    private Long userId;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private String orderType; // DELIVERY, PICKUP, DINE_IN
    private BigDecimal totalAmount;
    private String currency;
    private String paymentMethod;
    private String language;
    private List<OrderItemDto> items;
    private String deliveryAddress;
    private String estimatedTime;
    private String timestamp;
}
