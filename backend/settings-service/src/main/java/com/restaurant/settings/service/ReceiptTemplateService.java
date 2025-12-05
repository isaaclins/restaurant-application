package com.restaurant.settings.service;

import com.restaurant.settings.dto.UpdateReceiptTemplateRequest;
import com.restaurant.settings.entity.CurrencyPosition;
import com.restaurant.settings.entity.Language;
import com.restaurant.settings.entity.ReceiptTemplate;
import com.restaurant.settings.repository.ReceiptTemplateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Receipt Templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptTemplateService {

    private final ReceiptTemplateRepository receiptTemplateRepository;

    /**
     * Initialize default templates for all languages on startup
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultTemplates() {
        log.info("Initializing default receipt templates...");

        for (Language language : Language.values()) {
            if (!receiptTemplateRepository.existsByLanguage(language)) {
                ReceiptTemplate template = createDefaultTemplate(language);
                receiptTemplateRepository.save(template);
                log.info("Created default receipt template for language: {}", language);
            }
        }

        log.info("Receipt template initialization complete");
    }

    /**
     * Get all receipt templates
     */
    public List<ReceiptTemplate> getAllTemplates() {
        return receiptTemplateRepository.findAll();
    }

    /**
     * Get template by language
     */
    public ReceiptTemplate getTemplateByLanguage(Language language) {
        return receiptTemplateRepository.findByLanguage(language)
                .orElseGet(() -> {
                    ReceiptTemplate template = createDefaultTemplate(language);
                    return receiptTemplateRepository.save(template);
                });
    }

    /**
     * Get template by ID
     */
    public ReceiptTemplate getTemplateById(Long id) {
        return receiptTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt template not found: " + id));
    }

    /**
     * Get default template
     */
    public ReceiptTemplate getDefaultTemplate() {
        return receiptTemplateRepository.findByIsDefaultTrue()
                .orElseGet(() -> getTemplateByLanguage(Language.DE));
    }

    /**
     * Update template
     */
    @Transactional
    public ReceiptTemplate updateTemplate(Long id, UpdateReceiptTemplateRequest request) {
        ReceiptTemplate template = getTemplateById(id);

        // If setting as default, clear other defaults first
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(template.getIsDefault())) {
            receiptTemplateRepository.findByIsDefaultTrue().ifPresent(defaultTemplate -> {
                defaultTemplate.setIsDefault(false);
                receiptTemplateRepository.save(defaultTemplate);
            });
        }

        // Update fields if provided
        if (request.getIsDefault() != null)
            template.setIsDefault(request.getIsDefault());

        // Header
        if (request.getShowLogo() != null)
            template.setShowLogo(request.getShowLogo());
        if (request.getHeaderText() != null)
            template.setHeaderText(request.getHeaderText());
        if (request.getShowRestaurantInfo() != null)
            template.setShowRestaurantInfo(request.getShowRestaurantInfo());

        // Content
        if (request.getShowCustomerInfo() != null)
            template.setShowCustomerInfo(request.getShowCustomerInfo());
        if (request.getShowOrderNumber() != null)
            template.setShowOrderNumber(request.getShowOrderNumber());
        if (request.getShowDateTime() != null)
            template.setShowDateTime(request.getShowDateTime());
        if (request.getDateFormat() != null)
            template.setDateFormat(request.getDateFormat());
        if (request.getShowItemNotes() != null)
            template.setShowItemNotes(request.getShowItemNotes());
        if (request.getShowVatDetails() != null)
            template.setShowVatDetails(request.getShowVatDetails());
        if (request.getShowPaymentMethod() != null)
            template.setShowPaymentMethod(request.getShowPaymentMethod());

        // Footer
        if (request.getFooterText() != null)
            template.setFooterText(request.getFooterText());
        if (request.getThankYouMessage() != null)
            template.setThankYouMessage(request.getThankYouMessage());
        if (request.getShowSocialMedia() != null)
            template.setShowSocialMedia(request.getShowSocialMedia());
        if (request.getSocialMediaText() != null)
            template.setSocialMediaText(request.getSocialMediaText());
        if (request.getPromoText() != null)
            template.setPromoText(request.getPromoText());

        // Formatting
        if (request.getCurrency() != null)
            template.setCurrency(request.getCurrency());
        if (request.getCurrencyPosition() != null)
            template.setCurrencyPosition(request.getCurrencyPosition());
        if (request.getDecimalSeparator() != null)
            template.setDecimalSeparator(request.getDecimalSeparator());
        if (request.getThousandSeparator() != null)
            template.setThousandSeparator(request.getThousandSeparator());

        // Labels
        if (request.getLabelReceipt() != null)
            template.setLabelReceipt(request.getLabelReceipt());
        if (request.getLabelReceiptNumber() != null)
            template.setLabelReceiptNumber(request.getLabelReceiptNumber());
        if (request.getLabelOrderNumber() != null)
            template.setLabelOrderNumber(request.getLabelOrderNumber());
        if (request.getLabelDate() != null)
            template.setLabelDate(request.getLabelDate());
        if (request.getLabelOrderType() != null)
            template.setLabelOrderType(request.getLabelOrderType());
        if (request.getLabelPaymentMethod() != null)
            template.setLabelPaymentMethod(request.getLabelPaymentMethod());
        if (request.getLabelCustomer() != null)
            template.setLabelCustomer(request.getLabelCustomer());
        if (request.getLabelItem() != null)
            template.setLabelItem(request.getLabelItem());
        if (request.getLabelQuantity() != null)
            template.setLabelQuantity(request.getLabelQuantity());
        if (request.getLabelPrice() != null)
            template.setLabelPrice(request.getLabelPrice());
        if (request.getLabelTotal() != null)
            template.setLabelTotal(request.getLabelTotal());
        if (request.getLabelSubtotal() != null)
            template.setLabelSubtotal(request.getLabelSubtotal());
        if (request.getLabelVat() != null)
            template.setLabelVat(request.getLabelVat());
        if (request.getLabelDeliveryFee() != null)
            template.setLabelDeliveryFee(request.getLabelDeliveryFee());
        if (request.getLabelDiscount() != null)
            template.setLabelDiscount(request.getLabelDiscount());
        if (request.getLabelDelivery() != null)
            template.setLabelDelivery(request.getLabelDelivery());
        if (request.getLabelPickup() != null)
            template.setLabelPickup(request.getLabelPickup());
        if (request.getLabelDineIn() != null)
            template.setLabelDineIn(request.getLabelDineIn());

        return receiptTemplateRepository.save(template);
    }

    /**
     * Reset template to defaults
     */
    @Transactional
    public ReceiptTemplate resetTemplate(Long id) {
        ReceiptTemplate existing = getTemplateById(id);
        Language language = existing.getLanguage();
        Boolean wasDefault = existing.getIsDefault();

        ReceiptTemplate newTemplate = createDefaultTemplate(language);
        newTemplate.setId(existing.getId());
        newTemplate.setIsDefault(wasDefault);

        return receiptTemplateRepository.save(newTemplate);
    }

    /**
     * Create default template for a specific language
     */
    private ReceiptTemplate createDefaultTemplate(Language language) {
        ReceiptTemplate template = new ReceiptTemplate();
        template.setLanguage(language);
        template.setIsDefault(language == Language.DE); // German is default

        // Common settings
        template.setShowLogo(true);
        template.setShowRestaurantInfo(true);
        template.setShowCustomerInfo(true);
        template.setShowOrderNumber(true);
        template.setShowDateTime(true);
        template.setShowItemNotes(true);
        template.setShowVatDetails(true);
        template.setShowPaymentMethod(true);
        template.setShowSocialMedia(false);
        template.setCurrency("CHF");
        template.setCurrencyPosition(CurrencyPosition.BEFORE);

        // Language-specific content
        switch (language) {
            case DE -> setGermanDefaults(template);
            case EN -> setEnglishDefaults(template);
            case FR -> setFrenchDefaults(template);
            case IT -> setItalianDefaults(template);
        }

        return template;
    }

    private void setGermanDefaults(ReceiptTemplate template) {
        template.setDateFormat("dd.MM.yyyy HH:mm");
        template.setDecimalSeparator(".");
        template.setThousandSeparator("'");

        template.setHeaderText("Herzlich Willkommen!");
        template.setFooterText("Vielen Dank für Ihren Besuch!");
        template.setThankYouMessage("Wir freuen uns auf Ihren nächsten Besuch!");

        template.setLabelReceipt("Quittung");
        template.setLabelReceiptNumber("Quittungsnr.");
        template.setLabelOrderNumber("Bestellnr.");
        template.setLabelDate("Datum");
        template.setLabelOrderType("Bestellart");
        template.setLabelPaymentMethod("Zahlungsart");
        template.setLabelCustomer("Kunde");
        template.setLabelItem("Artikel");
        template.setLabelQuantity("Menge");
        template.setLabelPrice("Preis");
        template.setLabelTotal("Gesamt");
        template.setLabelSubtotal("Zwischensumme");
        template.setLabelVat("MwSt.");
        template.setLabelDeliveryFee("Liefergebühr");
        template.setLabelDiscount("Rabatt");
        template.setLabelDelivery("Lieferung");
        template.setLabelPickup("Abholung");
        template.setLabelDineIn("Im Lokal");
    }

    private void setEnglishDefaults(ReceiptTemplate template) {
        template.setDateFormat("MM/dd/yyyy hh:mm a");
        template.setDecimalSeparator(".");
        template.setThousandSeparator(",");

        template.setHeaderText("Welcome!");
        template.setFooterText("Thank you for your visit!");
        template.setThankYouMessage("We look forward to seeing you again!");

        template.setLabelReceipt("Receipt");
        template.setLabelReceiptNumber("Receipt No.");
        template.setLabelOrderNumber("Order No.");
        template.setLabelDate("Date");
        template.setLabelOrderType("Order Type");
        template.setLabelPaymentMethod("Payment Method");
        template.setLabelCustomer("Customer");
        template.setLabelItem("Item");
        template.setLabelQuantity("Qty");
        template.setLabelPrice("Price");
        template.setLabelTotal("Total");
        template.setLabelSubtotal("Subtotal");
        template.setLabelVat("VAT");
        template.setLabelDeliveryFee("Delivery Fee");
        template.setLabelDiscount("Discount");
        template.setLabelDelivery("Delivery");
        template.setLabelPickup("Pickup");
        template.setLabelDineIn("Dine In");
    }

    private void setFrenchDefaults(ReceiptTemplate template) {
        template.setDateFormat("dd/MM/yyyy HH:mm");
        template.setDecimalSeparator(",");
        template.setThousandSeparator(" ");

        template.setHeaderText("Bienvenue!");
        template.setFooterText("Merci de votre visite!");
        template.setThankYouMessage("Au plaisir de vous revoir!");

        template.setLabelReceipt("Reçu");
        template.setLabelReceiptNumber("N° de reçu");
        template.setLabelOrderNumber("N° de commande");
        template.setLabelDate("Date");
        template.setLabelOrderType("Type de commande");
        template.setLabelPaymentMethod("Mode de paiement");
        template.setLabelCustomer("Client");
        template.setLabelItem("Article");
        template.setLabelQuantity("Qté");
        template.setLabelPrice("Prix");
        template.setLabelTotal("Total");
        template.setLabelSubtotal("Sous-total");
        template.setLabelVat("TVA");
        template.setLabelDeliveryFee("Frais de livraison");
        template.setLabelDiscount("Remise");
        template.setLabelDelivery("Livraison");
        template.setLabelPickup("À emporter");
        template.setLabelDineIn("Sur place");
    }

    private void setItalianDefaults(ReceiptTemplate template) {
        template.setDateFormat("dd/MM/yyyy HH:mm");
        template.setDecimalSeparator(",");
        template.setThousandSeparator(".");

        template.setHeaderText("Benvenuti!");
        template.setFooterText("Grazie per la visita!");
        template.setThankYouMessage("Vi aspettiamo presto!");

        template.setLabelReceipt("Ricevuta");
        template.setLabelReceiptNumber("N. ricevuta");
        template.setLabelOrderNumber("N. ordine");
        template.setLabelDate("Data");
        template.setLabelOrderType("Tipo ordine");
        template.setLabelPaymentMethod("Metodo di pagamento");
        template.setLabelCustomer("Cliente");
        template.setLabelItem("Articolo");
        template.setLabelQuantity("Qtà");
        template.setLabelPrice("Prezzo");
        template.setLabelTotal("Totale");
        template.setLabelSubtotal("Subtotale");
        template.setLabelVat("IVA");
        template.setLabelDeliveryFee("Spese di consegna");
        template.setLabelDiscount("Sconto");
        template.setLabelDelivery("Consegna");
        template.setLabelPickup("Asporto");
        template.setLabelDineIn("Al tavolo");
    }
}
