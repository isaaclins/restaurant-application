package com.restaurant.order.entity;

/**
 * Order Status Enum
 */
public enum OrderStatus {
    PENDING, // Order received, awaiting confirmation
    CONFIRMED, // Order confirmed by restaurant
    IN_PROGRESS, // Order being prepared
    READY, // Order ready for pickup/delivery
    DELIVERED, // Order delivered to customer
    PICKED_UP, // Order picked up by customer
    CANCELLED // Order cancelled
}
