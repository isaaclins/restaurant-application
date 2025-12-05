package com.restaurant.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Daily Report DTO - Summary of daily sales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportResponse {

    private LocalDate date;

    // Order counts
    private int totalOrders;
    private int deliveryOrders;
    private int pickupOrders;
    private int dineInOrders;

    // Revenue
    private BigDecimal totalRevenue;
    private BigDecimal totalVat;
    private BigDecimal netRevenue;

    // Average
    private BigDecimal averageOrderValue;

    // Payment breakdown
    private Map<String, BigDecimal> revenueByPaymentMethod;

    // Top products
    private List<ProductSalesData> topProducts;

    // Hourly breakdown
    private Map<Integer, Integer> ordersByHour;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesData {
        private String productName;
        private int quantitySold;
        private BigDecimal revenue;
    }
}
