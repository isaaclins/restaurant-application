package com.restaurant.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.payment.dto.PaymentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Payment Controller Integration Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DECLINE_TEST_CARD = "4000000000000002";
    private static final String VALID_CARD = "4111111111111111";

    private PaymentRequest createValidPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setAmount(new BigDecimal("25.50"));
        request.setMethod("CARD");
        request.setCardNumber(VALID_CARD);
        request.setExpiryDate("12/25");
        request.setCvv("123");
        return request;
    }

    @Nested
    @DisplayName("POST /api/payments")
    class ProcessPayment {

        @Test
        @DisplayName("should process payment successfully")
        void shouldProcessPaymentSuccessfully() throws Exception {
            PaymentRequest request = createValidPaymentRequest();

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId", startsWith("PAY-")))
                    .andExpect(jsonPath("$.transactionId", startsWith("TXN-")))
                    .andExpect(jsonPath("$.status", is("COMPLETED")))
                    .andExpect(jsonPath("$.message", is("Payment successful")))
                    .andExpect(jsonPath("$.amount", is(25.50)))
                    .andExpect(jsonPath("$.processedAt", notNullValue()));
        }

        @Test
        @DisplayName("should decline payment with test decline card")
        void shouldDeclinePaymentWithTestCard() throws Exception {
            PaymentRequest request = createValidPaymentRequest();
            request.setCardNumber(DECLINE_TEST_CARD);

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isPaymentRequired()) // 402
                    .andExpect(jsonPath("$.paymentId", startsWith("PAY-")))
                    .andExpect(jsonPath("$.status", is("FAILED")))
                    .andExpect(jsonPath("$.message", is("Card declined")));
        }

        @Test
        @DisplayName("should process cash payment")
        void shouldProcessCashPayment() throws Exception {
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(1L);
            request.setAmount(new BigDecimal("15.00"));
            request.setMethod("CASH");

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("should return 400 when orderId is missing")
        void shouldReturn400WhenOrderIdMissing() throws Exception {
            PaymentRequest request = createValidPaymentRequest();
            request.setOrderId(null);

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when amount is missing")
        void shouldReturn400WhenAmountMissing() throws Exception {
            PaymentRequest request = createValidPaymentRequest();
            request.setAmount(null);

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when amount is negative")
        void shouldReturn400WhenAmountNegative() throws Exception {
            PaymentRequest request = createValidPaymentRequest();
            request.setAmount(new BigDecimal("-10.00"));

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when amount is zero")
        void shouldReturn400WhenAmountZero() throws Exception {
            PaymentRequest request = createValidPaymentRequest();
            request.setAmount(BigDecimal.ZERO);

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/payments/{id}")
    class GetPaymentStatus {

        @Test
        @DisplayName("should return payment status for valid ID")
        void shouldReturnPaymentStatus() throws Exception {
            String paymentId = "PAY-12345678";

            mockMvc.perform(get("/api/payments/{id}", paymentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId", is(paymentId)))
                    .andExpect(jsonPath("$.status", is("COMPLETED")))
                    .andExpect(jsonPath("$.message", is("Payment found")))
                    .andExpect(jsonPath("$.processedAt", notNullValue()));
        }

        @Test
        @DisplayName("should return payment status for any ID (mockup)")
        void shouldReturnStatusForAnyId() throws Exception {
            // Since this is a mockup, any ID should work
            mockMvc.perform(get("/api/payments/{id}", "PAY-NONEXISTENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }
    }
}
