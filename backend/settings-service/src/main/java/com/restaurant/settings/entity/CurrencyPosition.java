package com.restaurant.settings.entity;

/**
 * Position of currency symbol in formatted prices
 */
public enum CurrencyPosition {
    BEFORE("CHF 45.50"), // CHF 45.50
    AFTER("45.50 CHF"); // 45.50 CHF

    private final String example;

    CurrencyPosition(String example) {
        this.example = example;
    }

    public String getExample() {
        return example;
    }
}
