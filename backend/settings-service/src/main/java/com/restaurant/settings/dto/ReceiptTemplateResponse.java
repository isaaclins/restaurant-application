package com.restaurant.settings.dto;

import com.restaurant.settings.entity.CurrencyPosition;
import com.restaurant.settings.entity.Language;
import com.restaurant.settings.entity.ReceiptTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Receipt Template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptTemplateResponse {

    private Long id;
    private Language language;
    private String languageNativeName;
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

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReceiptTemplateResponse fromEntity(ReceiptTemplate template) {
        return ReceiptTemplateResponse.builder()
                .id(template.getId())
                .language(template.getLanguage())
                .languageNativeName(template.getLanguage().getNativeName())
                .isDefault(template.getIsDefault())
                // Header
                .showLogo(template.getShowLogo())
                .headerText(template.getHeaderText())
                .showRestaurantInfo(template.getShowRestaurantInfo())
                // Content
                .showCustomerInfo(template.getShowCustomerInfo())
                .showOrderNumber(template.getShowOrderNumber())
                .showDateTime(template.getShowDateTime())
                .dateFormat(template.getDateFormat())
                .showItemNotes(template.getShowItemNotes())
                .showVatDetails(template.getShowVatDetails())
                .showPaymentMethod(template.getShowPaymentMethod())
                // Footer
                .footerText(template.getFooterText())
                .thankYouMessage(template.getThankYouMessage())
                .showSocialMedia(template.getShowSocialMedia())
                .socialMediaText(template.getSocialMediaText())
                .promoText(template.getPromoText())
                // Formatting
                .currency(template.getCurrency())
                .currencyPosition(template.getCurrencyPosition())
                .decimalSeparator(template.getDecimalSeparator())
                .thousandSeparator(template.getThousandSeparator())
                // Labels
                .labelReceipt(template.getLabelReceipt())
                .labelReceiptNumber(template.getLabelReceiptNumber())
                .labelOrderNumber(template.getLabelOrderNumber())
                .labelDate(template.getLabelDate())
                .labelOrderType(template.getLabelOrderType())
                .labelPaymentMethod(template.getLabelPaymentMethod())
                .labelCustomer(template.getLabelCustomer())
                .labelItem(template.getLabelItem())
                .labelQuantity(template.getLabelQuantity())
                .labelPrice(template.getLabelPrice())
                .labelTotal(template.getLabelTotal())
                .labelSubtotal(template.getLabelSubtotal())
                .labelVat(template.getLabelVat())
                .labelDeliveryFee(template.getLabelDeliveryFee())
                .labelDiscount(template.getLabelDiscount())
                .labelDelivery(template.getLabelDelivery())
                .labelPickup(template.getLabelPickup())
                .labelDineIn(template.getLabelDineIn())
                // Timestamps
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
