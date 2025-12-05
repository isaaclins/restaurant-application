package com.restaurant.receipt.service;

import com.restaurant.receipt.config.RestaurantConfig;
import com.restaurant.receipt.dto.*;
import com.restaurant.receipt.entity.OrderType;
import com.restaurant.receipt.entity.Receipt;
import com.restaurant.receipt.entity.ReceiptItem;
import com.restaurant.receipt.exception.DuplicateReceiptException;
import com.restaurant.receipt.exception.ReceiptNotFoundException;
import com.restaurant.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Receipt Service - Business logic for receipt management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

        private final ReceiptRepository receiptRepository;
        private final PdfService pdfService;
        private final ImageService imageService;
        private final EscPosService escPosService;
        private final RestaurantConfig restaurantConfig;

        /**
         * Create a new receipt
         */
        @Transactional
        public ReceiptResponse createReceipt(CreateReceiptRequest request) {
                log.info("Creating receipt for order: {}", request.getOrderNumber());

                // Check if receipt already exists for this order
                if (receiptRepository.existsByOrderId(request.getOrderId())) {
                        throw new DuplicateReceiptException(request.getOrderId());
                }

                // Generate receipt number
                String receiptNumber = generateReceiptNumber();

                // Calculate VAT
                BigDecimal vatRate = restaurantConfig.getVatRate();
                BigDecimal vatAmount = calculateVat(request.getSubtotal(), vatRate);

                // Build receipt
                Receipt receipt = Receipt.builder()
                                .receiptNumber(receiptNumber)
                                .orderId(request.getOrderId())
                                .orderNumber(request.getOrderNumber())
                                .customerName(request.getCustomerName())
                                .customerEmail(request.getCustomerEmail())
                                .customerPhone(request.getCustomerPhone())
                                .customerId(request.getCustomerId())
                                .restaurantName(restaurantConfig.getName())
                                .restaurantAddress(restaurantConfig.getAddress())
                                .restaurantPhone(restaurantConfig.getPhone())
                                .vatNumber(restaurantConfig.getVatNumber())
                                .orderType(parseOrderType(request.getOrderType()))
                                .paymentMethod(request.getPaymentMethod())
                                .subtotal(request.getSubtotal())
                                .vatAmount(vatAmount)
                                .vatRate(vatRate)
                                .deliveryFee(request.getDeliveryFee())
                                .discount(request.getDiscount())
                                .totalAmount(request.getTotalAmount())
                                .build();

                // Add items
                for (CreateReceiptItemRequest itemRequest : request.getItems()) {
                        ReceiptItem item = ReceiptItem.builder()
                                        .productId(itemRequest.getProductId())
                                        .productName(itemRequest.getProductName())
                                        .productDescription(itemRequest.getProductDescription())
                                        .quantity(itemRequest.getQuantity())
                                        .unitPrice(itemRequest.getUnitPrice())
                                        .totalPrice(itemRequest.getTotalPrice())
                                        .notes(itemRequest.getNotes())
                                        .build();
                        receipt.addItem(item);
                }

                // Generate PDF
                byte[] pdfData = pdfService.generateReceiptPdf(receipt);
                receipt.setPdfData(pdfData);
                receipt.setPdfFilename("receipt-" + receiptNumber + ".pdf");

                Receipt saved = receiptRepository.save(receipt);
                log.info("Receipt created: {}", saved.getReceiptNumber());

                return mapToResponse(saved);
        }

        /**
         * Get receipt by ID
         */
        public ReceiptResponse getReceiptById(Long id) {
                Receipt receipt = receiptRepository.findById(id)
                                .orElseThrow(() -> new ReceiptNotFoundException(id));
                return mapToResponse(receipt);
        }

        /**
         * Get receipt by receipt number
         */
        public ReceiptResponse getReceiptByNumber(String receiptNumber) {
                Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found: " + receiptNumber));
                return mapToResponse(receipt);
        }

        /**
         * Get receipt by order ID
         */
        public ReceiptResponse getReceiptByOrderId(Long orderId) {
                Receipt receipt = receiptRepository.findByOrderId(orderId)
                                .orElseThrow(() -> new ReceiptNotFoundException(
                                                "Receipt not found for order: " + orderId));
                return mapToResponse(receipt);
        }

        /**
         * Get PDF for receipt
         */
        public byte[] getReceiptPdf(Long id) {
                Receipt receipt = receiptRepository.findById(id)
                                .orElseThrow(() -> new ReceiptNotFoundException(id));

                if (receipt.getPdfData() == null) {
                        // Regenerate PDF if not stored
                        byte[] pdfData = pdfService.generateReceiptPdf(receipt);
                        receipt.setPdfData(pdfData);
                        receipt.setPdfFilename("receipt-" + receipt.getReceiptNumber() + ".pdf");
                        receiptRepository.save(receipt);
                        return pdfData;
                }

                return receipt.getPdfData();
        }

        /**
         * Get PNG image for receipt
         */
        public byte[] getReceiptPng(Long id) {
                Receipt receipt = receiptRepository.findById(id)
                                .orElseThrow(() -> new ReceiptNotFoundException(id));
                return imageService.generateReceiptPng(receipt);
        }

        /**
         * Get high-quality PNG image for receipt (300 DPI)
         */
        public byte[] getReceiptPngHighQuality(Long id) {
                Receipt receipt = receiptRepository.findById(id)
                                .orElseThrow(() -> new ReceiptNotFoundException(id));
                return imageService.generateHighQualityPng(receipt);
        }

        /**
         * Get ESC/POS format for thermal printer
         */
        public byte[] getReceiptEscPos(Long id) {
                Receipt receipt = receiptRepository.findById(id)
                                .orElseThrow(() -> new ReceiptNotFoundException(id));
                return escPosService.generateEscPos(receipt);
        }

        /**
         * Get kitchen ticket in ESC/POS format
         */
        public byte[] getKitchenTicketEscPos(Long id) {
                Receipt receipt = receiptRepository.findById(id)
                                .orElseThrow(() -> new ReceiptNotFoundException(id));
                return escPosService.generateKitchenTicket(receipt);
        }

        /**
         * Get receipts for a customer
         */
        public List<ReceiptResponse> getReceiptsByCustomer(Long customerId) {
                return receiptRepository.findByCustomerId(customerId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get all receipts for a date
         */
        public List<ReceiptResponse> getReceiptsByDate(LocalDate date) {
                LocalDateTime dateTime = date.atStartOfDay();
                return receiptRepository.findByDate(dateTime).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Generate daily report
         */
        public DailyReportResponse generateDailyReport(LocalDate date) {
                log.info("Generating daily report for: {}", date);

                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

                List<Receipt> receipts = receiptRepository.findByCreatedAtBetween(startOfDay, endOfDay);

                if (receipts.isEmpty()) {
                        return DailyReportResponse.builder()
                                        .date(date)
                                        .totalOrders(0)
                                        .totalRevenue(BigDecimal.ZERO)
                                        .totalVat(BigDecimal.ZERO)
                                        .netRevenue(BigDecimal.ZERO)
                                        .averageOrderValue(BigDecimal.ZERO)
                                        .revenueByPaymentMethod(new HashMap<>())
                                        .topProducts(new ArrayList<>())
                                        .ordersByHour(new HashMap<>())
                                        .build();
                }

                // Calculate totals
                BigDecimal totalRevenue = receipts.stream()
                                .map(Receipt::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalVat = receipts.stream()
                                .map(r -> r.getVatAmount() != null ? r.getVatAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Order counts by type
                int deliveryOrders = (int) receipts.stream().filter(r -> r.getOrderType() == OrderType.DELIVERY)
                                .count();
                int pickupOrders = (int) receipts.stream().filter(r -> r.getOrderType() == OrderType.PICKUP).count();
                int dineInOrders = (int) receipts.stream().filter(r -> r.getOrderType() == OrderType.DINE_IN).count();

                // Revenue by payment method
                Map<String, BigDecimal> revenueByPayment = receipts.stream()
                                .filter(r -> r.getPaymentMethod() != null)
                                .collect(Collectors.groupingBy(
                                                Receipt::getPaymentMethod,
                                                Collectors.reducing(BigDecimal.ZERO, Receipt::getTotalAmount,
                                                                BigDecimal::add)));

                // Orders by hour
                Map<Integer, Integer> ordersByHour = receipts.stream()
                                .collect(Collectors.groupingBy(
                                                r -> r.getCreatedAt().getHour(),
                                                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

                // Top products
                Map<String, DailyReportResponse.ProductSalesData> productSales = new HashMap<>();
                for (Receipt receipt : receipts) {
                        for (ReceiptItem item : receipt.getItems()) {
                                String name = item.getProductName();
                                productSales.merge(name,
                                                DailyReportResponse.ProductSalesData.builder()
                                                                .productName(name)
                                                                .quantitySold(item.getQuantity())
                                                                .revenue(item.getTotalPrice())
                                                                .build(),
                                                (existing, newData) -> DailyReportResponse.ProductSalesData.builder()
                                                                .productName(name)
                                                                .quantitySold(existing.getQuantitySold()
                                                                                + newData.getQuantitySold())
                                                                .revenue(existing.getRevenue()
                                                                                .add(newData.getRevenue()))
                                                                .build());
                        }
                }

                List<DailyReportResponse.ProductSalesData> topProducts = productSales.values().stream()
                                .sorted((a, b) -> Integer.compare(b.getQuantitySold(), a.getQuantitySold()))
                                .limit(10)
                                .collect(Collectors.toList());

                BigDecimal avgOrderValue = totalRevenue.divide(
                                new BigDecimal(receipts.size()), 2, RoundingMode.HALF_UP);

                return DailyReportResponse.builder()
                                .date(date)
                                .totalOrders(receipts.size())
                                .deliveryOrders(deliveryOrders)
                                .pickupOrders(pickupOrders)
                                .dineInOrders(dineInOrders)
                                .totalRevenue(totalRevenue)
                                .totalVat(totalVat)
                                .netRevenue(totalRevenue.subtract(totalVat))
                                .averageOrderValue(avgOrderValue)
                                .revenueByPaymentMethod(revenueByPayment)
                                .topProducts(topProducts)
                                .ordersByHour(ordersByHour)
                                .build();
        }

        // Helper methods

        private String generateReceiptNumber() {
                long count = receiptRepository.count() + 1;
                return String.format("REC-%s-%06d",
                                LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
                                count);
        }

        private BigDecimal calculateVat(BigDecimal subtotal, BigDecimal vatRate) {
                return subtotal.multiply(vatRate)
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
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

        private ReceiptResponse mapToResponse(Receipt receipt) {
                List<ReceiptItemResponse> items = receipt.getItems().stream()
                                .map(item -> ReceiptItemResponse.builder()
                                                .productId(item.getProductId())
                                                .productName(item.getProductName())
                                                .productDescription(item.getProductDescription())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .totalPrice(item.getTotalPrice())
                                                .notes(item.getNotes())
                                                .build())
                                .collect(Collectors.toList());

                return ReceiptResponse.builder()
                                .id(receipt.getId())
                                .receiptNumber(receipt.getReceiptNumber())
                                .orderId(receipt.getOrderId())
                                .orderNumber(receipt.getOrderNumber())
                                .customerName(receipt.getCustomerName())
                                .customerEmail(receipt.getCustomerEmail())
                                .customerPhone(receipt.getCustomerPhone())
                                .restaurantName(receipt.getRestaurantName())
                                .restaurantAddress(receipt.getRestaurantAddress())
                                .restaurantPhone(receipt.getRestaurantPhone())
                                .vatNumber(receipt.getVatNumber())
                                .orderType(receipt.getOrderType() != null ? receipt.getOrderType().name() : null)
                                .paymentMethod(receipt.getPaymentMethod())
                                .subtotal(receipt.getSubtotal())
                                .vatAmount(receipt.getVatAmount())
                                .vatRate(receipt.getVatRate())
                                .deliveryFee(receipt.getDeliveryFee())
                                .discount(receipt.getDiscount())
                                .totalAmount(receipt.getTotalAmount())
                                .items(items)
                                .createdAt(receipt.getCreatedAt())
                                .pdfAvailable(receipt.getPdfData() != null)
                                .build();
        }
}
