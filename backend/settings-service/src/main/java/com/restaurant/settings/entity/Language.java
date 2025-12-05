package com.restaurant.settings.entity;

/**
 * Supported languages for receipts and notifications
 */
public enum Language {
    DE("Deutsch", "German"),
    EN("English", "English"),
    IT("Italiano", "Italian"),
    FR("Français", "French");

    private final String nativeName;
    private final String englishName;

    Language(String nativeName, String englishName) {
        this.nativeName = nativeName;
        this.englishName = englishName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public String getEnglishName() {
        return englishName;
    }
}
