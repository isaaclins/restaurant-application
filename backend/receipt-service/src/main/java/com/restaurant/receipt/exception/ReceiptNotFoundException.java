package com.restaurant.receipt.exception;

/**
 * Receipt Not Found Exception
 */
public class ReceiptNotFoundException extends RuntimeException {

    public ReceiptNotFoundException(String message) {
        super(message);
    }

    public ReceiptNotFoundException(Long id) {
        super("Receipt not found with id: " + id);
    }
}
