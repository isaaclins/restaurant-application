package com.restaurant.receipt.kafka;

import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import com.restaurant.receipt.entity.OrderType;
import com.restaurant.receipt.repository.ReceiptRepository;
import com.restaurant.receipt.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Kafka consumer for order events
 * Creates receipts automatically when orders are completed
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final ReceiptRepository receiptRepository;
    private final PdfService pdfService;

    @Value("${restaurant.name}")
    private String restaurantName;

    @Value("${restaurant.address}")
    private String restaurantAddress;

    @Value("${restaurant.phone}")
    private String restaurantPhone;

    @Value("${restaurant.vat-number}")
    private String vatNumber;

    @Value("${restaurant.vat-rate}")
    private BigDecimal vatRate;

    /**
     * Listen for completed orders and create receipts
     */
    @KafkaListener(topics = "order.completed", groupId = "receipt-service-group")
    public void handleOrderCompleted(OrderEvent event) {
        log.info("Received order.completed event for order: {}", event.getOrderNumber());

        try {
            // Check if receipt already exists
            if (receiptRepository.findByOrderId(event.getOrderId()).isPresent()) {
                log.warn("Receipt already exists for order: {}", event.getOrderNumber());
                return;
            }

            // Create receipt
            Receipt receipt = createReceipt(event);

            // Generate PDF
            byte[] pdfData = pdfService.generateReceiptPdf(receipt);
            receipt.setPdfData(pdfData);
            receipt.setPdfFilename(generateFilename(receipt));

            // Save receipt
            Receipt saved = receiptRepository.save(receipt);
            log.info("Created receipt {} for order {}", saved.getReceiptNumber(), event.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to create receipt for order {}: {}", event.getOrderNumber(), e.getMessage(), e);
        }
    }

    private Receipt createReceipt(OrderEvent event) {
        // Calculate VAT
        BigDecimal subtotal = event.getSubtotal() != null ? event.getSubtotal() : event.getTotalPrice();
        BigDecimal vatAmount = subtotal.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Receipt receipt = Receipt.builder()
                .receiptNumber(generateReceiptNumber())
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .customerName(event.getCustomerName())
                .customerEmail(event.getCustomerEmail())
                .customerPhone(event.getCustomerPhone())
                .customerId(event.getCustomerId())
                .restaurantName(restaurantName)
                .restaurantAddress(restaurantAddress)
                .restaurantPhone(restaurantPhone)
                .vatNumber(vatNumber)
                .orderType(parseOrderType(event.getOrderType()))
                .paymentMethod(event.getPaymentMethod())
                .subtotal(subtotal)
                .vatAmount(vatAmount)
                .vatRate(vatRate)
                .deliveryFee(event.getDeliveryFee())
                .discount(event.getDiscount())
                .totalAmount(event.getTotalPrice())
                .currency("CHF")
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        // Add items
        if (event.getItems() != null) {
            for (OrderEvent.OrderItemEvent itemEvent : event.getItems()) {
                ReceiptItem item = ReceiptItem.builder()
                        .productId(itemEvent.getProductId())
                        .productName(itemEvent.getProductName())
                        .quantity(itemEvent.getQuantity())
                        .unitPrice(itemEvent.getUnitPrice())
                        .totalPrice(itemEvent.getTotalPrice())
                        .notes(itemEvent.getNotes())
                        .build();
                receipt.addItem(item);
            }
        }

        return receipt;
    }

    private String generateReceiptNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 1000);
        return "RCP-" + timestamp + "-" + String.format("%03d", random);
    }

    private String generateFilename(Receipt receipt) {
        return "receipt_" + receipt.getReceiptNumber() + ".pdf";
    }

    private OrderType parseOrderType(String type) {
        if (type == null)
            return OrderType.PICKUP;
        try {
            return OrderType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OrderType.PICKUP;
        }
    }
}
