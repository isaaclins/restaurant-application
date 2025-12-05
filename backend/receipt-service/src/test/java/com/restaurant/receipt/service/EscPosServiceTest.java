package com.restaurant.receipt.service;

import com.restaurant.receipt.entity.OrderType;
import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class EscPosServiceTest {

    private EscPosService escPosService;
    private Receipt testReceipt;

    @BeforeEach
    void setUp() {
        escPosService = new EscPosService();

        testReceipt = Receipt.builder()
                .id(1L)
                .receiptNumber("REC-2025-0001")
                .orderNumber("ORD-001")
                .orderId(100L)
                .restaurantName("Test Restaurant")
                .restaurantAddress("Teststrasse 1, 8000 Zürich")
                .restaurantPhone("+41 44 123 45 67")
                .vatNumber("CHE-123.456.789")
                .customerName("Max Mustermann")
                .customerPhone("+41 79 123 45 67")
                .orderType(OrderType.DELIVERY)
                .paymentMethod("Kreditkarte")
                .subtotal(new BigDecimal("45.00"))
                .vatRate(new BigDecimal("7.7"))
                .vatAmount(new BigDecimal("3.47"))
                .deliveryFee(new BigDecimal("5.00"))
                .discount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("53.47"))
                .currency("CHF")
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        ReceiptItem item1 = ReceiptItem.builder()
                .productId(1L)
                .productName("Margherita Pizza")
                .quantity(2)
                .unitPrice(new BigDecimal("18.00"))
                .totalPrice(new BigDecimal("36.00"))
                .build();

        ReceiptItem item2 = ReceiptItem.builder()
                .productId(2L)
                .productName("Cola")
                .quantity(3)
                .unitPrice(new BigDecimal("3.00"))
                .totalPrice(new BigDecimal("9.00"))
                .notes("Ohne Eis")
                .build();

        testReceipt.addItem(item1);
        testReceipt.addItem(item2);
    }

    @Test
    void generateEscPos_shouldContainInitCommand() {
        byte[] result = escPosService.generateEscPos(testReceipt);

        // ESC @ is the init command
        assertThat(result[0]).isEqualTo((byte) 0x1B);
        assertThat(result[1]).isEqualTo((byte) '@');
    }

    @Test
    void generateEscPos_shouldContainRestaurantName() {
        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("Test Restaurant");
    }

    @Test
    void generateEscPos_shouldContainReceiptNumber() {
        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("REC-2025-0001");
    }

    @Test
    void generateEscPos_shouldContainOrderNumber() {
        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("ORD-001");
    }

    @Test
    void generateEscPos_shouldContainProductNames() {
        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("Margherita Pizza");
        assertThat(resultString).contains("Cola");
    }

    @Test
    void generateEscPos_shouldContainItemNotes() {
        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("Ohne Eis");
    }

    @Test
    void generateEscPos_shouldContainTotal() {
        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("53.47");
    }

    @Test
    void generateEscPos_shouldContainCutCommand() {
        byte[] result = escPosService.generateEscPos(testReceipt);

        // Check for GS V command (paper cut)
        boolean hasCutCommand = false;
        for (int i = 0; i < result.length - 2; i++) {
            if (result[i] == 0x1D && result[i + 1] == 'V') {
                hasCutCommand = true;
                break;
            }
        }
        assertThat(hasCutCommand).isTrue();
    }

    @Test
    void generateEscPos_shouldContainOrderType() {
        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("LIEFERUNG");
    }

    @Test
    void generateKitchenTicket_shouldContainOrderNumber() {
        byte[] result = escPosService.generateKitchenTicket(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("ORD-001");
    }

    @Test
    void generateKitchenTicket_shouldContainCustomerName() {
        byte[] result = escPosService.generateKitchenTicket(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("Max Mustermann");
    }

    @Test
    void generateKitchenTicket_shouldContainItems() {
        byte[] result = escPosService.generateKitchenTicket(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("2x Margherita Pizza");
        assertThat(resultString).contains("3x Cola");
    }

    @Test
    void generateKitchenTicket_shouldHighlightNotes() {
        byte[] result = escPosService.generateKitchenTicket(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        // Notes should be uppercase for kitchen visibility
        assertThat(resultString).contains("OHNE EIS");
    }

    @Test
    void generateKitchenTicket_shouldNotContainPrices() {
        byte[] result = escPosService.generateKitchenTicket(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        // Kitchen tickets should not show prices
        assertThat(resultString).doesNotContain("18.00");
        assertThat(resultString).doesNotContain("36.00");
        assertThat(resultString).doesNotContain("53.47");
    }

    @Test
    void generateEscPos_pickupOrder_shouldShowPickup() {
        testReceipt.setOrderType(OrderType.PICKUP);

        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("ABHOLUNG");
    }

    @Test
    void generateEscPos_dineInOrder_shouldShowDineIn() {
        testReceipt.setOrderType(OrderType.DINE_IN);

        byte[] result = escPosService.generateEscPos(testReceipt);
        String resultString = new String(result, StandardCharsets.UTF_8);

        assertThat(resultString).contains("IM LOKAL");
    }
}
