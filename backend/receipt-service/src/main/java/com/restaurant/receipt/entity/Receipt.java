package com.restaurant.receipt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Receipt Entity - Stores completed order receipts
 */
@Entity
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String receiptNumber;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String orderNumber;

    // Customer Info
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Long customerId;

    // Restaurant Info (snapshot at time of receipt)
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    private String vatNumber;

    // Order Details
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private String paymentMethod;

    // Pricing
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal vatAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    private String currency = "CHF";

    // Items as JSON or separate table
    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceiptItem> items = new ArrayList<>();

    // PDF storage
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] pdfData;

    private String pdfFilename;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void addItem(ReceiptItem item) {
        items.add(item);
        item.setReceipt(this);
    }

    // Alias for totalAmount
    public BigDecimal getTotal() {
        return totalAmount;
    }
}
