package com.restaurant.receipt.service;

import com.restaurant.receipt.entity.OrderType;
import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private ImageService imageService;

    private Receipt testReceipt;
    private byte[] samplePdfBytes;

    @BeforeEach
    void setUp() {
        testReceipt = Receipt.builder()
                .id(1L)
                .receiptNumber("REC-2025-0001")
                .orderNumber("ORD-001")
                .orderId(100L)
                .restaurantName("Test Restaurant")
                .restaurantAddress("Teststrasse 1, 8000 Zürich")
                .restaurantPhone("+41 44 123 45 67")
                .orderType(OrderType.PICKUP)
                .paymentMethod("Bar")
                .subtotal(new BigDecimal("20.00"))
                .totalAmount(new BigDecimal("20.00"))
                .currency("CHF")
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        ReceiptItem item = ReceiptItem.builder()
                .productId(1L)
                .productName("Test Product")
                .quantity(1)
                .unitPrice(new BigDecimal("20.00"))
                .totalPrice(new BigDecimal("20.00"))
                .build();

        testReceipt.addItem(item);

        // Minimal valid PDF bytes (would need real PDF for actual conversion)
        samplePdfBytes = "%PDF-1.4".getBytes();
    }

    @Test
    void generateReceiptPng_shouldCallPdfService() {
        when(pdfService.generateReceiptPdf(testReceipt)).thenReturn(samplePdfBytes);

        // This will fail with invalid PDF, but we can verify the flow
        assertThatThrownBy(() -> imageService.generateReceiptPng(testReceipt))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error");
    }

    @Test
    void convertPdfToPng_invalidPdf_shouldThrowException() {
        byte[] invalidPdf = "not a pdf".getBytes();

        assertThatThrownBy(() -> imageService.convertPdfToPng(invalidPdf))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error converting PDF to PNG");
    }

    @Test
    void generateHighQualityPng_shouldCallPdfService() {
        when(pdfService.generateReceiptPdf(testReceipt)).thenReturn(samplePdfBytes);

        assertThatThrownBy(() -> imageService.generateHighQualityPng(testReceipt))
                .isInstanceOf(RuntimeException.class);
    }

    // Integration test with real PDF would be:
    // @Test
    // void generateReceiptPng_withRealPdf_shouldReturnPngBytes() {
    //     // Generate real PDF first
    //     PdfService realPdfService = new PdfService();
    //     ImageService realImageService = new ImageService(realPdfService);
    //     
    //     byte[] result = realImageService.generateReceiptPng(testReceipt);
    //     
    //     assertThat(result).isNotEmpty();
    //     // PNG magic bytes
    //     assertThat(result[0]).isEqualTo((byte) 0x89);
    //     assertThat(result[1]).isEqualTo((byte) 0x50); // P
    //     assertThat(result[2]).isEqualTo((byte) 0x4E); // N
    //     assertThat(result[3]).isEqualTo((byte) 0x47); // G
    // }
}
