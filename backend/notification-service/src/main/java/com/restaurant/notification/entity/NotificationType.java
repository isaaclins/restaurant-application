package com.restaurant.notification.entity;

/**
 * Notification types based on trigger events
 */
public enum NotificationType {
    // Order related
    ORDER_CONFIRMATION,
    ORDER_PREPARING,
    ORDER_READY,
    ORDER_OUT_FOR_DELIVERY,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    
    // Payment related
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    REFUND_PROCESSED,
    
    // Receipt
    RECEIPT_READY,
    
    // Account related
    WELCOME,
    PASSWORD_RESET,
    EMAIL_VERIFICATION,
    ACCOUNT_UPDATED,
    
    // Marketing
    PROMOTION,
    NEWSLETTER,
    
    // System
    SYSTEM_ALERT
}
