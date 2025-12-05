package com.restaurant.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receipt events received via Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptEvent {
    private String receiptId;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String customerEmail;
    private String customerName;
    private String pdfUrl;
    private String pngUrl;
    private String language;
    private String timestamp;
}
