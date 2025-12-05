package com.restaurant.receipt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.receipt.dto.CreateReceiptItemRequest;
import com.restaurant.receipt.dto.CreateReceiptRequest;
import com.restaurant.receipt.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Receipt Controller Integration Tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReceiptRepository receiptRepository;

    @BeforeEach
    void setUp() {
        receiptRepository.deleteAll();
    }

    @Test
    void createReceipt_Success() throws Exception {
        CreateReceiptRequest request = createTestReceiptRequest(1L, "ORD-001");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.receiptNumber").isNotEmpty())
                .andExpect(jsonPath("$.customerName").value("Max Muster"))
                .andExpect(jsonPath("$.totalAmount").value(45.00))
                .andExpect(jsonPath("$.pdfAvailable").value(true))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void createReceipt_DuplicateOrderId_ReturnsConflict() throws Exception {
        CreateReceiptRequest request = createTestReceiptRequest(2L, "ORD-002");

        // Create first receipt
        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to create duplicate
        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void createReceipt_ValidationError_ReturnsBadRequest() throws Exception {
        CreateReceiptRequest request = new CreateReceiptRequest();
        // Missing required fields

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void getReceiptById_Success() throws Exception {
        // Create receipt first
        CreateReceiptRequest request = createTestReceiptRequest(3L, "ORD-003");
        String response = mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long receiptId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/receipts/{id}", receiptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receiptId))
                .andExpect(jsonPath("$.orderId").value(3));
    }

    @Test
    void getReceiptById_NotFound() throws Exception {
        mockMvc.perform(get("/api/receipts/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void getReceiptByOrderId_Success() throws Exception {
        CreateReceiptRequest request = createTestReceiptRequest(4L, "ORD-004");
        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/receipts/order/{orderId}", 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(4))
                .andExpect(jsonPath("$.orderNumber").value("ORD-004"));
    }

    @Test
    void getReceiptPdf_Success() throws Exception {
        CreateReceiptRequest request = createTestReceiptRequest(5L, "ORD-005");
        String response = mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long receiptId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/receipts/{id}/pdf", receiptId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void getReceiptsByCustomer_Success() throws Exception {
        Long customerId = 100L;

        // Create receipts for customer
        CreateReceiptRequest request1 = createTestReceiptRequest(6L, "ORD-006");
        request1.setCustomerId(customerId);

        CreateReceiptRequest request2 = createTestReceiptRequest(7L, "ORD-007");
        request2.setCustomerId(customerId);

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/receipts/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getReceiptsByDate_Success() throws Exception {
        CreateReceiptRequest request = createTestReceiptRequest(8L, "ORD-008");
        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/receipts/date/{date}", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getDailyReport_Success() throws Exception {
        // Create some receipts
        CreateReceiptRequest request1 = createTestReceiptRequest(9L, "ORD-009");
        request1.setOrderType("DELIVERY");
        request1.setPaymentMethod("CARD");

        CreateReceiptRequest request2 = createTestReceiptRequest(10L, "ORD-010");
        request2.setOrderType("PICKUP");
        request2.setPaymentMethod("CASH");

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/receipts/report/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.totalOrders").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalRevenue").isNumber())
                .andExpect(jsonPath("$.deliveryOrders").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.pickupOrders").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getDailyReportByDate_EmptyDay() throws Exception {
        LocalDate pastDate = LocalDate.of(2020, 1, 1);

        mockMvc.perform(get("/api/receipts/report/daily/{date}", pastDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(pastDate.toString()))
                .andExpect(jsonPath("$.totalOrders").value(0))
                .andExpect(jsonPath("$.totalRevenue").value(0));
    }

    @Test
    void getReceiptByNumber_Success() throws Exception {
        CreateReceiptRequest request = createTestReceiptRequest(11L, "ORD-011");
        String response = mockMvc.perform(post("/api/receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String receiptNumber = objectMapper.readTree(response).get("receiptNumber").asText();

        mockMvc.perform(get("/api/receipts/number/{receiptNumber}", receiptNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").value(receiptNumber));
    }

    // Helper method
    private CreateReceiptRequest createTestReceiptRequest(Long orderId, String orderNumber) {
        return CreateReceiptRequest.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .customerName("Max Muster")
                .customerEmail("max@example.com")
                .customerPhone("+41 79 123 45 67")
                .orderType("DELIVERY")
                .paymentMethod("CARD")
                .subtotal(new BigDecimal("40.00"))
                .deliveryFee(new BigDecimal("5.00"))
                .totalAmount(new BigDecimal("45.00"))
                .items(List.of(
                        CreateReceiptItemRequest.builder()
                                .productId(1L)
                                .productName("Margherita Pizza")
                                .quantity(2)
                                .unitPrice(new BigDecimal("15.00"))
                                .totalPrice(new BigDecimal("30.00"))
                                .build(),
                        CreateReceiptItemRequest.builder()
                                .productId(2L)
                                .productName("Coca Cola")
                                .quantity(2)
                                .unitPrice(new BigDecimal("5.00"))
                                .totalPrice(new BigDecimal("10.00"))
                                .build()))
                .build();
    }
}
