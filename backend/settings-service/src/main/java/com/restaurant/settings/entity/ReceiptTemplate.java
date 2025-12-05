package com.restaurant.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Receipt Template Entity - Customizable receipt layout per language
 */
@Entity
@Table(name = "receipt_templates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"language"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Language language;

    @Builder.Default
    private Boolean isDefault = false;

    // ==================== Header Section ====================
    
    @Builder.Default
    private Boolean showLogo = true;

    @Column(length = 500)
    private String headerText;

    @Builder.Default
    private Boolean showRestaurantInfo = true;

    // ==================== Content Section ====================
    
    @Builder.Default
    private Boolean showCustomerInfo = true;

    @Builder.Default
    private Boolean showOrderNumber = true;

    @Builder.Default
    private Boolean showDateTime = true;

    @Builder.Default
    @Column(length = 50)
    private String dateFormat = "dd.MM.yyyy HH:mm";

    @Builder.Default
    private Boolean showItemNotes = true;

    @Builder.Default
    private Boolean showVatDetails = true;

    @Builder.Default
    private Boolean showPaymentMethod = true;

    // ==================== Footer Section ====================
    
    @Column(length = 500)
    private String footerText;

    @Column(length = 500)
    private String thankYouMessage;

    @Builder.Default
    private Boolean showSocialMedia = false;

    @Column(length = 500)
    private String socialMediaText;

    @Column(length = 500)
    private String promoText;

    // ==================== Formatting ====================
    
    @Builder.Default
    @Column(length = 10)
    private String currency = "CHF";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CurrencyPosition currencyPosition = CurrencyPosition.BEFORE;

    @Builder.Default
    @Column(length = 5)
    private String decimalSeparator = ".";

    @Builder.Default
    @Column(length = 5)
    private String thousandSeparator = "'";

    // ==================== Localized Labels ====================
    
    // These are the translatable labels used in the receipt
    @Column(length = 100)
    private String labelReceipt;          // "QUITTUNG" / "RECEIPT" / "RICEVUTA" / "REÇU"

    @Column(length = 100)
    private String labelReceiptNumber;    // "Quittungsnr." / "Receipt No." / etc.

    @Column(length = 100)
    private String labelOrderNumber;      // "Bestellnr." / "Order No." / etc.

    @Column(length = 100)
    private String labelDate;             // "Datum" / "Date" / etc.

    @Column(length = 100)
    private String labelOrderType;        // "Bestellart" / "Order Type" / etc.

    @Column(length = 100)
    private String labelPaymentMethod;    // "Zahlungsart" / "Payment Method" / etc.

    @Column(length = 100)
    private String labelCustomer;         // "Kunde" / "Customer" / etc.

    @Column(length = 100)
    private String labelItem;             // "Artikel" / "Item" / etc.

    @Column(length = 100)
    private String labelQuantity;         // "Menge" / "Qty" / etc.

    @Column(length = 100)
    private String labelPrice;            // "Preis" / "Price" / etc.

    @Column(length = 100)
    private String labelTotal;            // "Total" / "Total" / etc.

    @Column(length = 100)
    private String labelSubtotal;         // "Zwischensumme" / "Subtotal" / etc.

    @Column(length = 100)
    private String labelVat;              // "MwSt." / "VAT" / etc.

    @Column(length = 100)
    private String labelDeliveryFee;      // "Liefergebühr" / "Delivery Fee" / etc.

    @Column(length = 100)
    private String labelDiscount;         // "Rabatt" / "Discount" / etc.

    @Column(length = 100)
    private String labelDelivery;         // "Lieferung" / "Delivery" / etc.

    @Column(length = 100)
    private String labelPickup;           // "Abholung" / "Pickup" / etc.

    @Column(length = 100)
    private String labelDineIn;           // "Im Restaurant" / "Dine-In" / etc.

    // ==================== Timestamps ====================
    
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
