package com.restaurant.receipt.service;

import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import com.restaurant.receipt.entity.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PDF Service Tests
 */
@SpringBootTest
@ActiveProfiles("test")
class PdfServiceTest {

    @Autowired
    private PdfService pdfService;

    @Test
    void generateReceiptPdf_Success() {
        Receipt receipt = createTestReceipt();

        byte[] pdfData = pdfService.generateReceiptPdf(receipt);

        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
        // PDF files start with %PDF
        assertTrue(new String(Arrays.copyOf(pdfData, 4)).startsWith("%PDF"));
    }

    @Test
    void generateReceiptPdf_WithAllFields() {
        Receipt receipt = createTestReceipt();
        receipt.setDeliveryFee(new BigDecimal("5.00"));
        receipt.setDiscount(new BigDecimal("3.00"));
        receipt.setVatAmount(new BigDecimal("3.29"));
        receipt.setVatRate(new BigDecimal("7.7"));

        byte[] pdfData = pdfService.generateReceiptPdf(receipt);

        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
    }

    @Test
    void generateReceiptPdf_MinimalReceipt() {
        Receipt receipt = Receipt.builder()
                .receiptNumber("REC-001")
                .orderId(1L)
                .orderNumber("ORD-001")
                .restaurantName("Test Restaurant")
                .restaurantAddress("Test Street 1")
                .restaurantPhone("+41 44 000 00 00")
                .orderType(OrderType.PICKUP)
                .subtotal(new BigDecimal("20.00"))
                .totalAmount(new BigDecimal("20.00"))
                .createdAt(LocalDateTime.now())
                .build();

        ReceiptItem item = ReceiptItem.builder()
                .productName("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("20.00"))
                .totalPrice(new BigDecimal("20.00"))
                .build();
        receipt.addItem(item);

        byte[] pdfData = pdfService.generateReceiptPdf(receipt);

        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
    }

    @Test
    void generateReceiptPdf_WithItemNotes() {
        Receipt receipt = createTestReceipt();
        receipt.getItems().get(0).setNotes("Extra cheese, no onions");

        byte[] pdfData = pdfService.generateReceiptPdf(receipt);

        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
    }

    @Test
    void generateReceiptPdf_DifferentOrderTypes() {
        Receipt receipt = createTestReceipt();

        // Test DELIVERY
        receipt.setOrderType(OrderType.DELIVERY);
        byte[] deliveryPdf = pdfService.generateReceiptPdf(receipt);
        assertNotNull(deliveryPdf);

        // Test PICKUP
        receipt.setOrderType(OrderType.PICKUP);
        byte[] pickupPdf = pdfService.generateReceiptPdf(receipt);
        assertNotNull(pickupPdf);

        // Test DINE_IN
        receipt.setOrderType(OrderType.DINE_IN);
        byte[] dineInPdf = pdfService.generateReceiptPdf(receipt);
        assertNotNull(dineInPdf);
    }

    private Receipt createTestReceipt() {
        Receipt receipt = Receipt.builder()
                .receiptNumber("REC-20250107-000001")
                .orderId(1L)
                .orderNumber("ORD-20250107-001")
                .customerName("Max Muster")
                .customerEmail("max@example.com")
                .customerPhone("+41 79 123 45 67")
                .restaurantName("Test Restaurant")
                .restaurantAddress("Teststrasse 1, 8000 Zürich")
                .restaurantPhone("+41 44 999 99 99")
                .vatNumber("CHE-999.999.999")
                .orderType(OrderType.DELIVERY)
                .paymentMethod("CARD")
                .subtotal(new BigDecimal("42.50"))
                .vatAmount(new BigDecimal("3.27"))
                .vatRate(new BigDecimal("7.7"))
                .totalAmount(new BigDecimal("42.50"))
                .createdAt(LocalDateTime.now())
                .build();

        ReceiptItem item1 = ReceiptItem.builder()
                .productId(1L)
                .productName("Margherita Pizza")
                .productDescription("Classic Italian pizza")
                .quantity(2)
                .unitPrice(new BigDecimal("18.50"))
                .totalPrice(new BigDecimal("37.00"))
                .build();

        ReceiptItem item2 = ReceiptItem.builder()
                .productId(2L)
                .productName("Coca Cola 0.5L")
                .quantity(1)
                .unitPrice(new BigDecimal("5.50"))
                .totalPrice(new BigDecimal("5.50"))
                .build();

        receipt.addItem(item1);
        receipt.addItem(item2);

        return receipt;
    }
}
