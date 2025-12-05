package com.restaurant.receipt.exception;

/**
 * Duplicate Receipt Exception
 */
public class DuplicateReceiptException extends RuntimeException {

    public DuplicateReceiptException(String message) {
        super(message);
    }

    public DuplicateReceiptException(Long orderId) {
        super("Receipt already exists for order: " + orderId);
    }
}
