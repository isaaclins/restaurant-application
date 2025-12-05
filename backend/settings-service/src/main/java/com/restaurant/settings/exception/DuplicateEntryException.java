package com.restaurant.settings.exception;

/**
 * Duplicate Entry Exception
 */
public class DuplicateEntryException extends RuntimeException {

    public DuplicateEntryException(String message) {
        super(message);
    }
}
