package com.restaurant.settings.exception;

/**
 * Settings Not Found Exception
 */
public class SettingsNotFoundException extends RuntimeException {

    public SettingsNotFoundException(String message) {
        super(message);
    }
}
