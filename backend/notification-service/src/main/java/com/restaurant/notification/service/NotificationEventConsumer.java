package com.restaurant.notification.service;

import com.restaurant.notification.dto.OrderEvent;
import com.restaurant.notification.dto.ReceiptEvent;
import com.restaurant.notification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka event consumers for notification triggers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final EmailService emailService;

    /**
     * Handle order events
     */
    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for order {}", event.getStatus(), event.getOrderNumber());

        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("No customer email for order {}, skipping notification", event.getOrderNumber());
            return;
        }

        NotificationType type = mapOrderStatusToNotificationType(event.getStatus());
        if (type == null) {
            log.debug("No notification type for order status: {}", event.getStatus());
            return;
        }

        Map<String, Object> variables = buildOrderVariables(event);

        emailService.sendEmail(
                type,
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getLanguage(),
                variables,
                event.getOrderId(),
                event.getUserId(),
                null
        );
    }

    /**
     * Handle payment events
     */
    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentEvent(OrderEvent event) {
        log.info("Received payment event for order {}", event.getOrderNumber());

        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("No customer email for payment on order {}, skipping notification", event.getOrderNumber());
            return;
        }

        Map<String, Object> variables = buildOrderVariables(event);

        emailService.sendEmail(
                NotificationType.PAYMENT_RECEIVED,
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getLanguage(),
                variables,
                event.getOrderId(),
                event.getUserId(),
                null
        );
    }

    /**
     * Handle receipt events
     */
    @KafkaListener(topics = "receipt-events", groupId = "notification-service-group")
    public void handleReceiptEvent(ReceiptEvent event) {
        log.info("Received receipt event for order {}", event.getOrderNumber());

        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("No customer email for receipt {}, skipping notification", event.getReceiptId());
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", event.getCustomerName());
        variables.put("orderNumber", event.getOrderNumber());
        variables.put("receiptId", event.getReceiptId());
        variables.put("pdfUrl", event.getPdfUrl());
        variables.put("pngUrl", event.getPngUrl());
        variables.put("year", LocalDateTime.now().getYear());

        emailService.sendEmail(
                NotificationType.RECEIPT_READY,
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getLanguage(),
                variables,
                event.getOrderId(),
                event.getUserId(),
                event.getReceiptId()
        );
    }

    private NotificationType mapOrderStatusToNotificationType(String status) {
        if (status == null) return null;
        
        return switch (status.toUpperCase()) {
            case "CONFIRMED", "CREATED" -> NotificationType.ORDER_CONFIRMATION;
            case "PREPARING" -> NotificationType.ORDER_PREPARING;
            case "READY" -> NotificationType.ORDER_READY;
            case "OUT_FOR_DELIVERY" -> NotificationType.ORDER_OUT_FOR_DELIVERY;
            case "DELIVERED", "COMPLETED" -> NotificationType.ORDER_DELIVERED;
            case "CANCELLED" -> NotificationType.ORDER_CANCELLED;
            default -> null;
        };
    }

    private Map<String, Object> buildOrderVariables(OrderEvent event) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", event.getCustomerName() != null ? event.getCustomerName() : "Kunde");
        variables.put("orderNumber", event.getOrderNumber());
        variables.put("orderId", event.getOrderId());
        variables.put("orderType", translateOrderType(event.getOrderType(), event.getLanguage()));
        variables.put("totalAmount", event.getTotalAmount());
        variables.put("currency", event.getCurrency() != null ? event.getCurrency() : "CHF");
        variables.put("paymentMethod", event.getPaymentMethod());
        variables.put("deliveryAddress", event.getDeliveryAddress());
        variables.put("estimatedTime", event.getEstimatedTime());
        variables.put("year", LocalDateTime.now().getYear());
        return variables;
    }

    private String translateOrderType(String orderType, String language) {
        if (orderType == null) return "";
        
        String lang = language != null ? language.toUpperCase() : "DE";
        
        return switch (orderType.toUpperCase()) {
            case "DELIVERY" -> switch (lang) {
                case "EN" -> "Delivery";
                case "FR" -> "Livraison";
                case "IT" -> "Consegna";
                default -> "Lieferung";
            };
            case "PICKUP" -> switch (lang) {
                case "EN" -> "Pickup";
                case "FR" -> "À emporter";
                case "IT" -> "Asporto";
                default -> "Abholung";
            };
            case "DINE_IN" -> switch (lang) {
                case "EN" -> "Dine In";
                case "FR" -> "Sur place";
                case "IT" -> "Al tavolo";
                default -> "Im Lokal";
            };
            default -> orderType;
        };
    }
}
