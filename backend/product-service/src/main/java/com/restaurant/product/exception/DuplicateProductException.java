package com.restaurant.product.exception;

/**
 * Exception thrown when trying to create a duplicate product
 */
public class DuplicateProductException extends RuntimeException {

    public DuplicateProductException(String message) {
        super(message);
    }
}
