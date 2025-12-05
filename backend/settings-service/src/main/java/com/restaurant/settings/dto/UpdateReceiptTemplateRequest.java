package com.restaurant.settings.dto;

import com.restaurant.settings.entity.CurrencyPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating Receipt Template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReceiptTemplateRequest {

    private Boolean isDefault;

    // Header
    private Boolean showLogo;
    private String headerText;
    private Boolean showRestaurantInfo;

    // Content
    private Boolean showCustomerInfo;
    private Boolean showOrderNumber;
    private Boolean showDateTime;
    private String dateFormat;
    private Boolean showItemNotes;
    private Boolean showVatDetails;
    private Boolean showPaymentMethod;

    // Footer
    private String footerText;
    private String thankYouMessage;
    private Boolean showSocialMedia;
    private String socialMediaText;
    private String promoText;

    // Formatting
    private String currency;
    private CurrencyPosition currencyPosition;
    private String decimalSeparator;
    private String thousandSeparator;

    // Labels
    private String labelReceipt;
    private String labelReceiptNumber;
    private String labelOrderNumber;
    private String labelDate;
    private String labelOrderType;
    private String labelPaymentMethod;
    private String labelCustomer;
    private String labelItem;
    private String labelQuantity;
    private String labelPrice;
    private String labelTotal;
    private String labelSubtotal;
    private String labelVat;
    private String labelDeliveryFee;
    private String labelDiscount;
    private String labelDelivery;
    private String labelPickup;
    private String labelDineIn;
}
