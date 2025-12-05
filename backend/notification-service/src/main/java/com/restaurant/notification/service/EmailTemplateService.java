package com.restaurant.notification.service;

import com.restaurant.notification.entity.EmailTemplate;
import com.restaurant.notification.entity.NotificationType;
import com.restaurant.notification.repository.EmailTemplateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing email templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final SpringTemplateEngine templateEngine;

    @PostConstruct
    @Transactional
    public void initializeDefaultTemplates() {
        log.info("Initializing default email templates...");

        String[] languages = { "DE", "EN", "FR", "IT" };

        for (String language : languages) {
            createDefaultTemplatesForLanguage(language);
        }

        log.info("Email template initialization complete");
    }

    private void createDefaultTemplatesForLanguage(String language) {
        // Order Confirmation
        if (!emailTemplateRepository.existsByTypeAndLanguage(NotificationType.ORDER_CONFIRMATION, language)) {
            emailTemplateRepository.save(createOrderConfirmationTemplate(language));
        }

        // Receipt Ready
        if (!emailTemplateRepository.existsByTypeAndLanguage(NotificationType.RECEIPT_READY, language)) {
            emailTemplateRepository.save(createReceiptReadyTemplate(language));
        }

        // Order Ready
        if (!emailTemplateRepository.existsByTypeAndLanguage(NotificationType.ORDER_READY, language)) {
            emailTemplateRepository.save(createOrderReadyTemplate(language));
        }

        // Payment Received
        if (!emailTemplateRepository.existsByTypeAndLanguage(NotificationType.PAYMENT_RECEIVED, language)) {
            emailTemplateRepository.save(createPaymentReceivedTemplate(language));
        }

        // Welcome
        if (!emailTemplateRepository.existsByTypeAndLanguage(NotificationType.WELCOME, language)) {
            emailTemplateRepository.save(createWelcomeTemplate(language));
        }

        // Password Reset
        if (!emailTemplateRepository.existsByTypeAndLanguage(NotificationType.PASSWORD_RESET, language)) {
            emailTemplateRepository.save(createPasswordResetTemplate(language));
        }

        // Promotion
        if (!emailTemplateRepository.existsByTypeAndLanguage(NotificationType.PROMOTION, language)) {
            emailTemplateRepository.save(createPromotionTemplate(language));
        }
    }

    public Optional<EmailTemplate> getTemplate(NotificationType type, String language) {
        return emailTemplateRepository.findByTypeAndLanguageAndIsActiveTrue(type, language)
                .or(() -> emailTemplateRepository.findByTypeAndLanguageAndIsActiveTrue(type, "DE"));
    }

    public EmailTemplate getTemplateById(Long id) {
        return emailTemplateRepository.findById(id).orElse(null);
    }

    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAll();
    }

    public List<EmailTemplate> getTemplatesByLanguage(String language) {
        return emailTemplateRepository.findByLanguage(language);
    }

    @Transactional
    public EmailTemplate updateTemplate(Long id, String subject, String htmlTemplate, String textTemplate) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));

        template.setSubject(subject);
        template.setHtmlTemplate(htmlTemplate);
        template.setTextTemplate(textTemplate);

        return emailTemplateRepository.save(template);
    }

    public String renderTemplate(EmailTemplate template, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        try {
            return templateEngine.process(template.getHtmlTemplate(), context);
        } catch (Exception e) {
            log.error("Failed to render template {}: {}", template.getId(), e.getMessage());
            // Return the template with simple variable replacement as fallback
            String result = template.getHtmlTemplate();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                result = result.replace("${" + entry.getKey() + "}",
                        entry.getValue() != null ? entry.getValue().toString() : "");
            }
            return result;
        }
    }

    // Default template creators
    private EmailTemplate createOrderConfirmationTemplate(String language) {
        String subject;
        String html;

        switch (language) {
            case "EN" -> {
                subject = "Order Confirmation - #{orderNumber}";
                html = getOrderConfirmationHtmlEn();
            }
            case "FR" -> {
                subject = "Confirmation de commande - N°{orderNumber}";
                html = getOrderConfirmationHtmlFr();
            }
            case "IT" -> {
                subject = "Conferma ordine - N.{orderNumber}";
                html = getOrderConfirmationHtmlIt();
            }
            default -> {
                subject = "Bestellbestätigung - Nr.{orderNumber}";
                html = getOrderConfirmationHtmlDe();
            }
        }

        return EmailTemplate.builder()
                .type(NotificationType.ORDER_CONFIRMATION)
                .language(language)
                .name("Order Confirmation - " + language)
                .subject(subject)
                .htmlTemplate(html)
                .isActive(true)
                .build();
    }

    private EmailTemplate createReceiptReadyTemplate(String language) {
        String subject;
        String html;

        switch (language) {
            case "EN" -> {
                subject = "Your Receipt - Order #{orderNumber}";
                html = getReceiptReadyHtmlEn();
            }
            case "FR" -> {
                subject = "Votre reçu - Commande N°{orderNumber}";
                html = getReceiptReadyHtmlFr();
            }
            case "IT" -> {
                subject = "La tua ricevuta - Ordine N.{orderNumber}";
                html = getReceiptReadyHtmlIt();
            }
            default -> {
                subject = "Ihre Quittung - Bestellung Nr.{orderNumber}";
                html = getReceiptReadyHtmlDe();
            }
        }

        return EmailTemplate.builder()
                .type(NotificationType.RECEIPT_READY)
                .language(language)
                .name("Receipt Ready - " + language)
                .subject(subject)
                .htmlTemplate(html)
                .isActive(true)
                .build();
    }

    private EmailTemplate createOrderReadyTemplate(String language) {
        String subject;
        String html;

        switch (language) {
            case "EN" -> {
                subject = "Your Order is Ready! - #{orderNumber}";
                html = getOrderReadyHtmlEn();
            }
            case "FR" -> {
                subject = "Votre commande est prête! - N°{orderNumber}";
                html = getOrderReadyHtmlFr();
            }
            case "IT" -> {
                subject = "Il tuo ordine è pronto! - N.{orderNumber}";
                html = getOrderReadyHtmlIt();
            }
            default -> {
                subject = "Ihre Bestellung ist bereit! - Nr.{orderNumber}";
                html = getOrderReadyHtmlDe();
            }
        }

        return EmailTemplate.builder()
                .type(NotificationType.ORDER_READY)
                .language(language)
                .name("Order Ready - " + language)
                .subject(subject)
                .htmlTemplate(html)
                .isActive(true)
                .build();
    }

    private EmailTemplate createPaymentReceivedTemplate(String language) {
        String subject;
        String html;

        switch (language) {
            case "EN" -> {
                subject = "Payment Received - Order #{orderNumber}";
                html = getPaymentReceivedHtmlEn();
            }
            case "FR" -> {
                subject = "Paiement reçu - Commande N°{orderNumber}";
                html = getPaymentReceivedHtmlFr();
            }
            case "IT" -> {
                subject = "Pagamento ricevuto - Ordine N.{orderNumber}";
                html = getPaymentReceivedHtmlIt();
            }
            default -> {
                subject = "Zahlung erhalten - Bestellung Nr.{orderNumber}";
                html = getPaymentReceivedHtmlDe();
            }
        }

        return EmailTemplate.builder()
                .type(NotificationType.PAYMENT_RECEIVED)
                .language(language)
                .name("Payment Received - " + language)
                .subject(subject)
                .htmlTemplate(html)
                .isActive(true)
                .build();
    }

    // HTML Templates - German
    private String getOrderConfirmationHtmlDe() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .order-details { background: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Bestellbestätigung</h1>
                        </div>
                        <div class="content">
                            <p>Hallo ${customerName},</p>
                            <p>Vielen Dank für Ihre Bestellung! Wir haben Ihre Bestellung erhalten und bereiten sie vor.</p>
                            <div class="order-details">
                                <h3>Bestelldetails</h3>
                                <p><strong>Bestellnummer:</strong> ${orderNumber}</p>
                                <p><strong>Bestellart:</strong> ${orderType}</p>
                                <p><strong>Gesamtbetrag:</strong> ${totalAmount} ${currency}</p>
                            </div>
                            <p>Sie erhalten eine weitere E-Mail, sobald Ihre Bestellung bereit ist.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Alle Rechte vorbehalten.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getReceiptReadyHtmlDe() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 10px 20px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Ihre Quittung</h1>
                        </div>
                        <div class="content">
                            <p>Hallo ${customerName},</p>
                            <p>Ihre Quittung für Bestellung <strong>${orderNumber}</strong> ist bereit.</p>
                            <p style="text-align: center; margin: 20px 0;">
                                <a href="${pdfUrl}" class="btn">PDF herunterladen</a>
                                <a href="${pngUrl}" class="btn">Als Bild</a>
                            </p>
                            <p>Vielen Dank für Ihren Einkauf!</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Alle Rechte vorbehalten.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getOrderReadyHtmlDe() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #FF9800; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .highlight { font-size: 24px; color: #FF9800; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Bestellung bereit!</h1>
                        </div>
                        <div class="content">
                            <p>Hallo ${customerName},</p>
                            <div class="highlight">
                                🎉 Ihre Bestellung ist bereit!
                            </div>
                            <p><strong>Bestellnummer:</strong> ${orderNumber}</p>
                            <p>Sie können Ihre Bestellung jetzt abholen.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Alle Rechte vorbehalten.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPaymentReceivedHtmlDe() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .amount { font-size: 28px; color: #4CAF50; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Zahlung erhalten</h1>
                        </div>
                        <div class="content">
                            <p>Hallo ${customerName},</p>
                            <p>Wir haben Ihre Zahlung erfolgreich erhalten.</p>
                            <div class="amount">
                                ✓ ${totalAmount} ${currency}
                            </div>
                            <p><strong>Bestellnummer:</strong> ${orderNumber}</p>
                            <p><strong>Zahlungsart:</strong> ${paymentMethod}</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Alle Rechte vorbehalten.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    // HTML Templates - English
    private String getOrderConfirmationHtmlEn() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .order-details { background: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Order Confirmation</h1>
                        </div>
                        <div class="content">
                            <p>Hello ${customerName},</p>
                            <p>Thank you for your order! We have received your order and are preparing it.</p>
                            <div class="order-details">
                                <h3>Order Details</h3>
                                <p><strong>Order Number:</strong> ${orderNumber}</p>
                                <p><strong>Order Type:</strong> ${orderType}</p>
                                <p><strong>Total Amount:</strong> ${totalAmount} ${currency}</p>
                            </div>
                            <p>You will receive another email when your order is ready.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getReceiptReadyHtmlEn() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 10px 20px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Your Receipt</h1>
                        </div>
                        <div class="content">
                            <p>Hello ${customerName},</p>
                            <p>Your receipt for order <strong>${orderNumber}</strong> is ready.</p>
                            <p style="text-align: center; margin: 20px 0;">
                                <a href="${pdfUrl}" class="btn">Download PDF</a>
                                <a href="${pngUrl}" class="btn">As Image</a>
                            </p>
                            <p>Thank you for your purchase!</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getOrderReadyHtmlEn() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #FF9800; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .highlight { font-size: 24px; color: #FF9800; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Order Ready!</h1>
                        </div>
                        <div class="content">
                            <p>Hello ${customerName},</p>
                            <div class="highlight">
                                🎉 Your order is ready!
                            </div>
                            <p><strong>Order Number:</strong> ${orderNumber}</p>
                            <p>You can pick up your order now.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPaymentReceivedHtmlEn() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .amount { font-size: 28px; color: #4CAF50; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Payment Received</h1>
                        </div>
                        <div class="content">
                            <p>Hello ${customerName},</p>
                            <p>We have successfully received your payment.</p>
                            <div class="amount">
                                ✓ ${totalAmount} ${currency}
                            </div>
                            <p><strong>Order Number:</strong> ${orderNumber}</p>
                            <p><strong>Payment Method:</strong> ${paymentMethod}</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    // HTML Templates - French
    private String getOrderConfirmationHtmlFr() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .order-details { background: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Confirmation de commande</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour ${customerName},</p>
                            <p>Merci pour votre commande! Nous avons reçu votre commande et la préparons.</p>
                            <div class="order-details">
                                <h3>Détails de la commande</h3>
                                <p><strong>Numéro de commande:</strong> ${orderNumber}</p>
                                <p><strong>Type de commande:</strong> ${orderType}</p>
                                <p><strong>Montant total:</strong> ${totalAmount} ${currency}</p>
                            </div>
                            <p>Vous recevrez un autre e-mail lorsque votre commande sera prête.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getReceiptReadyHtmlFr() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 10px 20px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Votre reçu</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour ${customerName},</p>
                            <p>Votre reçu pour la commande <strong>${orderNumber}</strong> est prêt.</p>
                            <p style="text-align: center; margin: 20px 0;">
                                <a href="${pdfUrl}" class="btn">Télécharger PDF</a>
                                <a href="${pngUrl}" class="btn">En image</a>
                            </p>
                            <p>Merci pour votre achat!</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getOrderReadyHtmlFr() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #FF9800; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .highlight { font-size: 24px; color: #FF9800; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Commande prête!</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour ${customerName},</p>
                            <div class="highlight">
                                🎉 Votre commande est prête!
                            </div>
                            <p><strong>Numéro de commande:</strong> ${orderNumber}</p>
                            <p>Vous pouvez récupérer votre commande maintenant.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPaymentReceivedHtmlFr() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .amount { font-size: 28px; color: #4CAF50; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Paiement reçu</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour ${customerName},</p>
                            <p>Nous avons bien reçu votre paiement.</p>
                            <div class="amount">
                                ✓ ${totalAmount} ${currency}
                            </div>
                            <p><strong>Numéro de commande:</strong> ${orderNumber}</p>
                            <p><strong>Mode de paiement:</strong> ${paymentMethod}</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    // HTML Templates - Italian
    private String getOrderConfirmationHtmlIt() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .order-details { background: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Conferma ordine</h1>
                        </div>
                        <div class="content">
                            <p>Ciao ${customerName},</p>
                            <p>Grazie per il tuo ordine! Abbiamo ricevuto il tuo ordine e lo stiamo preparando.</p>
                            <div class="order-details">
                                <h3>Dettagli ordine</h3>
                                <p><strong>Numero ordine:</strong> ${orderNumber}</p>
                                <p><strong>Tipo ordine:</strong> ${orderType}</p>
                                <p><strong>Importo totale:</strong> ${totalAmount} ${currency}</p>
                            </div>
                            <p>Riceverai un'altra email quando il tuo ordine sarà pronto.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tutti i diritti riservati.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getReceiptReadyHtmlIt() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 10px 20px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>La tua ricevuta</h1>
                        </div>
                        <div class="content">
                            <p>Ciao ${customerName},</p>
                            <p>La tua ricevuta per l'ordine <strong>${orderNumber}</strong> è pronta.</p>
                            <p style="text-align: center; margin: 20px 0;">
                                <a href="${pdfUrl}" class="btn">Scarica PDF</a>
                                <a href="${pngUrl}" class="btn">Come immagine</a>
                            </p>
                            <p>Grazie per il tuo acquisto!</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tutti i diritti riservati.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getOrderReadyHtmlIt() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #FF9800; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .highlight { font-size: 24px; color: #FF9800; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Ordine pronto!</h1>
                        </div>
                        <div class="content">
                            <p>Ciao ${customerName},</p>
                            <div class="highlight">
                                🎉 Il tuo ordine è pronto!
                            </div>
                            <p><strong>Numero ordine:</strong> ${orderNumber}</p>
                            <p>Puoi ritirare il tuo ordine adesso.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tutti i diritti riservati.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPaymentReceivedHtmlIt() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .amount { font-size: 28px; color: #4CAF50; text-align: center; padding: 20px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Pagamento ricevuto</h1>
                        </div>
                        <div class="content">
                            <p>Ciao ${customerName},</p>
                            <p>Abbiamo ricevuto con successo il tuo pagamento.</p>
                            <div class="amount">
                                ✓ ${totalAmount} ${currency}
                            </div>
                            <p><strong>Numero ordine:</strong> ${orderNumber}</p>
                            <p><strong>Metodo di pagamento:</strong> ${paymentMethod}</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} Restaurant. Tutti i diritti riservati.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    // ==================== WELCOME TEMPLATES ====================

    private EmailTemplate createWelcomeTemplate(String language) {
        String subject;
        String html;

        switch (language) {
            case "EN" -> {
                subject = "Welcome to ${restaurantName}!";
                html = getWelcomeHtmlEn();
            }
            case "FR" -> {
                subject = "Bienvenue chez ${restaurantName}!";
                html = getWelcomeHtmlFr();
            }
            case "IT" -> {
                subject = "Benvenuto da ${restaurantName}!";
                html = getWelcomeHtmlIt();
            }
            default -> {
                subject = "Willkommen bei ${restaurantName}!";
                html = getWelcomeHtmlDe();
            }
        }

        return EmailTemplate.builder()
                .type(NotificationType.WELCOME)
                .language(language)
                .name("Welcome - " + language)
                .subject(subject)
                .htmlTemplate(html)
                .isActive(true)
                .build();
    }

    private String getWelcomeHtmlDe() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #FF6B35, #FF9800); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #FF6B35; color: white; text-decoration: none; border-radius: 25px; font-weight: bold; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Willkommen!</h1>
                        </div>
                        <div class="content">
                            <p>Hallo ${customerName},</p>
                            <p>Herzlich willkommen bei <strong>${restaurantName}</strong>! Wir freuen uns, Sie als neuen Kunden begrüssen zu dürfen.</p>
                            <p>Als Dankeschön für Ihre Registrierung schenken wir Ihnen <strong>10% Rabatt</strong> auf Ihre erste Bestellung!</p>
                            <p style="text-align: center; margin: 30px 0;">
                                <a href="${orderUrl}" class="btn">Jetzt bestellen</a>
                            </p>
                            <p>Bei Fragen stehen wir Ihnen gerne zur Verfügung.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Alle Rechte vorbehalten.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getWelcomeHtmlEn() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #FF6B35, #FF9800); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #FF6B35; color: white; text-decoration: none; border-radius: 25px; font-weight: bold; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Welcome!</h1>
                        </div>
                        <div class="content">
                            <p>Hello ${customerName},</p>
                            <p>Welcome to <strong>${restaurantName}</strong>! We're excited to have you as our new customer.</p>
                            <p>As a thank you for registering, we're giving you <strong>10% off</strong> your first order!</p>
                            <p style="text-align: center; margin: 30px 0;">
                                <a href="${orderUrl}" class="btn">Order Now</a>
                            </p>
                            <p>If you have any questions, we're here to help.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getWelcomeHtmlFr() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #FF6B35, #FF9800); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #FF6B35; color: white; text-decoration: none; border-radius: 25px; font-weight: bold; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Bienvenue!</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour ${customerName},</p>
                            <p>Bienvenue chez <strong>${restaurantName}</strong>! Nous sommes ravis de vous accueillir parmi nos clients.</p>
                            <p>Pour vous remercier de votre inscription, nous vous offrons <strong>10% de réduction</strong> sur votre première commande!</p>
                            <p style="text-align: center; margin: 30px 0;">
                                <a href="${orderUrl}" class="btn">Commander maintenant</a>
                            </p>
                            <p>Pour toute question, nous sommes à votre disposition.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getWelcomeHtmlIt() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #FF6B35, #FF9800); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #FF6B35; color: white; text-decoration: none; border-radius: 25px; font-weight: bold; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Benvenuto!</h1>
                        </div>
                        <div class="content">
                            <p>Ciao ${customerName},</p>
                            <p>Benvenuto da <strong>${restaurantName}</strong>! Siamo felici di averti come nuovo cliente.</p>
                            <p>Come ringraziamento per la registrazione, ti offriamo <strong>10% di sconto</strong> sul tuo primo ordine!</p>
                            <p style="text-align: center; margin: 30px 0;">
                                <a href="${orderUrl}" class="btn">Ordina ora</a>
                            </p>
                            <p>Per qualsiasi domanda, siamo a tua disposizione.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Tutti i diritti riservati.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    // ==================== PASSWORD RESET TEMPLATES ====================

    private EmailTemplate createPasswordResetTemplate(String language) {
        String subject;
        String html;

        switch (language) {
            case "EN" -> {
                subject = "Reset Your Password - ${restaurantName}";
                html = getPasswordResetHtmlEn();
            }
            case "FR" -> {
                subject = "Réinitialisation de votre mot de passe - ${restaurantName}";
                html = getPasswordResetHtmlFr();
            }
            case "IT" -> {
                subject = "Reimposta la tua password - ${restaurantName}";
                html = getPasswordResetHtmlIt();
            }
            default -> {
                subject = "Passwort zurücksetzen - ${restaurantName}";
                html = getPasswordResetHtmlDe();
            }
        }

        return EmailTemplate.builder()
                .type(NotificationType.PASSWORD_RESET)
                .language(language)
                .name("Password Reset - " + language)
                .subject(subject)
                .htmlTemplate(html)
                .isActive(true)
                .build();
    }

    private String getPasswordResetHtmlDe() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #2196F3; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }
                        .code { background: #e0e0e0; padding: 15px 30px; font-size: 24px; letter-spacing: 5px; text-align: center; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .warning { color: #666; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Passwort zurücksetzen</h1>
                        </div>
                        <div class="content">
                            <p>Hallo ${customerName},</p>
                            <p>Sie haben eine Anfrage zum Zurücksetzen Ihres Passworts gestellt. Klicken Sie auf den Button unten oder verwenden Sie den Code:</p>
                            <div class="code">${resetCode}</div>
                            <p style="text-align: center;">
                                <a href="${resetUrl}" class="btn">Passwort zurücksetzen</a>
                            </p>
                            <p class="warning">Dieser Link ist 24 Stunden gültig. Wenn Sie diese Anfrage nicht gestellt haben, ignorieren Sie diese E-Mail bitte.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Alle Rechte vorbehalten.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPasswordResetHtmlEn() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #2196F3; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }
                        .code { background: #e0e0e0; padding: 15px 30px; font-size: 24px; letter-spacing: 5px; text-align: center; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .warning { color: #666; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Reset Your Password</h1>
                        </div>
                        <div class="content">
                            <p>Hello ${customerName},</p>
                            <p>You requested to reset your password. Click the button below or use the code:</p>
                            <div class="code">${resetCode}</div>
                            <p style="text-align: center;">
                                <a href="${resetUrl}" class="btn">Reset Password</a>
                            </p>
                            <p class="warning">This link is valid for 24 hours. If you didn't request this, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPasswordResetHtmlFr() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #2196F3; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }
                        .code { background: #e0e0e0; padding: 15px 30px; font-size: 24px; letter-spacing: 5px; text-align: center; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .warning { color: #666; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Réinitialiser le mot de passe</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour ${customerName},</p>
                            <p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous ou utilisez le code:</p>
                            <div class="code">${resetCode}</div>
                            <p style="text-align: center;">
                                <a href="${resetUrl}" class="btn">Réinitialiser</a>
                            </p>
                            <p class="warning">Ce lien est valide 24 heures. Si vous n'avez pas fait cette demande, veuillez ignorer cet e-mail.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPasswordResetHtmlIt() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background: #f9f9f9; }
                        .btn { display: inline-block; padding: 12px 30px; background: #2196F3; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }
                        .code { background: #e0e0e0; padding: 15px 30px; font-size: 24px; letter-spacing: 5px; text-align: center; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .warning { color: #666; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Reimposta la password</h1>
                        </div>
                        <div class="content">
                            <p>Ciao ${customerName},</p>
                            <p>Hai richiesto di reimpostare la password. Clicca sul pulsante qui sotto o usa il codice:</p>
                            <div class="code">${resetCode}</div>
                            <p style="text-align: center;">
                                <a href="${resetUrl}" class="btn">Reimposta password</a>
                            </p>
                            <p class="warning">Questo link è valido per 24 ore. Se non hai fatto questa richiesta, ignora questa email.</p>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Tutti i diritti riservati.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    // ==================== PROMOTION TEMPLATES ====================

    private EmailTemplate createPromotionTemplate(String language) {
        String subject;
        String html;

        switch (language) {
            case "EN" -> {
                subject = "🔥 Special Offer from ${restaurantName}!";
                html = getPromotionHtmlEn();
            }
            case "FR" -> {
                subject = "🔥 Offre spéciale de ${restaurantName}!";
                html = getPromotionHtmlFr();
            }
            case "IT" -> {
                subject = "🔥 Offerta speciale da ${restaurantName}!";
                html = getPromotionHtmlIt();
            }
            default -> {
                subject = "🔥 Sonderangebot von ${restaurantName}!";
                html = getPromotionHtmlDe();
            }
        }

        return EmailTemplate.builder()
                .type(NotificationType.PROMOTION)
                .language(language)
                .name("Promotion - " + language)
                .subject(subject)
                .htmlTemplate(html)
                .isActive(true)
                .build();
    }

    private String getPromotionHtmlDe() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #e91e63, #ff5722); color: white; padding: 40px; text-align: center; border-radius: 10px 10px 0 0; }
                        .discount { font-size: 48px; font-weight: bold; }
                        .content { padding: 30px; background: #f9f9f9; text-align: center; }
                        .btn { display: inline-block; padding: 15px 40px; background: #e91e63; color: white; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 18px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .valid { background: #fff3e0; padding: 10px; border-radius: 5px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="discount">${discountPercent}% RABATT</div>
                            <p>Exklusiv für Sie!</p>
                        </div>
                        <div class="content">
                            <h2>${promoTitle}</h2>
                            <p>${promoDescription}</p>
                            <div class="valid">
                                <strong>Gültig bis:</strong> ${validUntil}<br>
                                <strong>Code:</strong> ${promoCode}
                            </div>
                            <a href="${orderUrl}" class="btn">Jetzt bestellen</a>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Alle Rechte vorbehalten.</p>
                            <p><a href="${unsubscribeUrl}">Abmelden</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPromotionHtmlEn() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #e91e63, #ff5722); color: white; padding: 40px; text-align: center; border-radius: 10px 10px 0 0; }
                        .discount { font-size: 48px; font-weight: bold; }
                        .content { padding: 30px; background: #f9f9f9; text-align: center; }
                        .btn { display: inline-block; padding: 15px 40px; background: #e91e63; color: white; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 18px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .valid { background: #fff3e0; padding: 10px; border-radius: 5px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="discount">${discountPercent}% OFF</div>
                            <p>Exclusive for you!</p>
                        </div>
                        <div class="content">
                            <h2>${promoTitle}</h2>
                            <p>${promoDescription}</p>
                            <div class="valid">
                                <strong>Valid until:</strong> ${validUntil}<br>
                                <strong>Code:</strong> ${promoCode}
                            </div>
                            <a href="${orderUrl}" class="btn">Order Now</a>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. All rights reserved.</p>
                            <p><a href="${unsubscribeUrl}">Unsubscribe</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPromotionHtmlFr() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #e91e63, #ff5722); color: white; padding: 40px; text-align: center; border-radius: 10px 10px 0 0; }
                        .discount { font-size: 48px; font-weight: bold; }
                        .content { padding: 30px; background: #f9f9f9; text-align: center; }
                        .btn { display: inline-block; padding: 15px 40px; background: #e91e63; color: white; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 18px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .valid { background: #fff3e0; padding: 10px; border-radius: 5px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="discount">${discountPercent}% DE RÉDUCTION</div>
                            <p>Exclusif pour vous!</p>
                        </div>
                        <div class="content">
                            <h2>${promoTitle}</h2>
                            <p>${promoDescription}</p>
                            <div class="valid">
                                <strong>Valable jusqu'au:</strong> ${validUntil}<br>
                                <strong>Code:</strong> ${promoCode}
                            </div>
                            <a href="${orderUrl}" class="btn">Commander</a>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Tous droits réservés.</p>
                            <p><a href="${unsubscribeUrl}">Se désabonner</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getPromotionHtmlIt() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #e91e63, #ff5722); color: white; padding: 40px; text-align: center; border-radius: 10px 10px 0 0; }
                        .discount { font-size: 48px; font-weight: bold; }
                        .content { padding: 30px; background: #f9f9f9; text-align: center; }
                        .btn { display: inline-block; padding: 15px 40px; background: #e91e63; color: white; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 18px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                        .valid { background: #fff3e0; padding: 10px; border-radius: 5px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="discount">${discountPercent}% DI SCONTO</div>
                            <p>Esclusivo per te!</p>
                        </div>
                        <div class="content">
                            <h2>${promoTitle}</h2>
                            <p>${promoDescription}</p>
                            <div class="valid">
                                <strong>Valido fino al:</strong> ${validUntil}<br>
                                <strong>Codice:</strong> ${promoCode}
                            </div>
                            <a href="${orderUrl}" class="btn">Ordina ora</a>
                        </div>
                        <div class="footer">
                            <p>© ${year} ${restaurantName}. Tutti i diritti riservati.</p>
                            <p><a href="${unsubscribeUrl}">Annulla iscrizione</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
