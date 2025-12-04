package com.restaurant.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.order.dto.*;
import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.OrderItem;
import com.restaurant.order.entity.OrderStatus;
import com.restaurant.order.entity.OrderType;
import com.restaurant.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Controller Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    private Order createTestOrder(String customerName, OrderType orderType, OrderStatus status) {
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerName(customerName)
                .customerEmail(customerName.toLowerCase().replace(" ", "") + "@example.com")
                .customerPhone("+41791234567")
                .orderType(orderType)
                .status(status)
                .paymentMethod("CARD")
                .totalPrice(new BigDecimal("25.50"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productName("Margherita Pizza")
                .quantity(2)
                .unitPrice(new BigDecimal("12.75"))
                .totalPrice(new BigDecimal("25.50"))
                .build();
        item.setOrder(order);
        order.getItems().add(item);

        return orderRepository.save(order);
    }

    private CreateOrderRequest buildCreateOrderRequest() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setProductName("Margherita Pizza");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("12.75"));
        item.setTotalPrice(new BigDecimal("25.50"));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john.doe@example.com");
        request.setCustomerPhone("+41791234567");
        request.setOrderType(OrderType.PICKUP);
        request.setPaymentMethod("CARD");
        request.setItems(List.of(item));
        request.setTotalPrice(new BigDecimal("25.50"));

        return request;
    }

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrder {

        @Test
        @DisplayName("should create order successfully")
        void shouldCreateOrder() throws Exception {
            CreateOrderRequest request = buildCreateOrderRequest();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.customerName", is("John Doe")))
                    .andExpect(jsonPath("$.orderType", is("PICKUP")))
                    .andExpect(jsonPath("$.status", is("PENDING")))
                    .andExpect(jsonPath("$.totalPrice", is(25.50)))
                    .andExpect(jsonPath("$.orderNumber", notNullValue()));

            assertThat(orderRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should create delivery order with address")
        void shouldCreateDeliveryOrderWithAddress() throws Exception {
            CreateOrderRequest request = buildCreateOrderRequest();
            request.setOrderType(OrderType.DELIVERY);

            AddressDTO address = new AddressDTO();
            address.setStreet("123 Main Street");
            address.setCity("Zurich");
            address.setPostalCode("8001");
            request.setDeliveryAddress(address);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderType", is("DELIVERY")));
        }

        @Test
        @DisplayName("should return 400 when customer name is missing")
        void shouldReturn400WhenCustomerNameMissing() throws Exception {
            CreateOrderRequest request = buildCreateOrderRequest();
            request.setCustomerName(null);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when order type is missing")
        void shouldReturn400WhenOrderTypeMissing() throws Exception {
            CreateOrderRequest request = buildCreateOrderRequest();
            request.setOrderType(null);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when total price is missing")
        void shouldReturn400WhenTotalPriceMissing() throws Exception {
            CreateOrderRequest request = buildCreateOrderRequest();
            request.setTotalPrice(null);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderById {

        @Test
        @DisplayName("should return order when exists")
        void shouldReturnOrderWhenExists() throws Exception {
            Order order = createTestOrder("John Doe", OrderType.PICKUP, OrderStatus.PENDING);

            mockMvc.perform(get("/api/orders/{id}", order.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(order.getId().intValue())))
                    .andExpect(jsonPath("$.customerName", is("John Doe")))
                    .andExpect(jsonPath("$.orderType", is("PICKUP")))
                    .andExpect(jsonPath("$.status", is("PENDING")));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/orders/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/orders")
    class GetAllOrders {

        @Test
        @DisplayName("should return empty list when no orders")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return all orders")
        void shouldReturnAllOrders() throws Exception {
            createTestOrder("Customer 1", OrderType.PICKUP, OrderStatus.PENDING);
            createTestOrder("Customer 2", OrderType.DELIVERY, OrderStatus.CONFIRMED);

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("should filter orders by status")
        void shouldFilterByStatus() throws Exception {
            createTestOrder("Customer 1", OrderType.PICKUP, OrderStatus.PENDING);
            createTestOrder("Customer 2", OrderType.DELIVERY, OrderStatus.CONFIRMED);
            createTestOrder("Customer 3", OrderType.DINE_IN, OrderStatus.PENDING);

            mockMvc.perform(get("/api/orders")
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("should filter orders by date")
        void shouldFilterByDate() throws Exception {
            createTestOrder("Customer 1", OrderType.PICKUP, OrderStatus.PENDING);
            createTestOrder("Customer 2", OrderType.DELIVERY, OrderStatus.CONFIRMED);

            mockMvc.perform(get("/api/orders")
                            .param("date", LocalDate.now().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("PUT /api/orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status from PENDING to CONFIRMED")
        void shouldUpdateStatusToConfirmed() throws Exception {
            Order order = createTestOrder("John Doe", OrderType.PICKUP, OrderStatus.PENDING);

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(OrderStatus.CONFIRMED);

            mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("CONFIRMED")));

            Order updated = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("should update order status through full lifecycle")
        void shouldUpdateStatusThroughLifecycle() throws Exception {
            Order order = createTestOrder("John Doe", OrderType.PICKUP, OrderStatus.PENDING);

            // PENDING -> CONFIRMED
            UpdateStatusRequest confirmRequest = new UpdateStatusRequest(OrderStatus.CONFIRMED);
            mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("CONFIRMED")));

            // CONFIRMED -> IN_PROGRESS
            UpdateStatusRequest progressRequest = new UpdateStatusRequest(OrderStatus.IN_PROGRESS);
            mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(progressRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

            // IN_PROGRESS -> READY
            UpdateStatusRequest readyRequest = new UpdateStatusRequest(OrderStatus.READY);
            mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(readyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("READY")));

            // READY -> PICKED_UP
            UpdateStatusRequest pickedUpRequest = new UpdateStatusRequest(OrderStatus.PICKED_UP);
            mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pickedUpRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("PICKED_UP")));
        }

        @Test
        @DisplayName("should allow cancellation of pending order")
        void shouldAllowCancellation() throws Exception {
            Order order = createTestOrder("John Doe", OrderType.PICKUP, OrderStatus.PENDING);

            UpdateStatusRequest request = new UpdateStatusRequest(OrderStatus.CANCELLED);

            mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("CANCELLED")));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UpdateStatusRequest request = new UpdateStatusRequest(OrderStatus.CONFIRMED);

            mockMvc.perform(put("/api/orders/{id}/status", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when status is null")
        void shouldReturn400WhenStatusNull() throws Exception {
            Order order = createTestOrder("John Doe", OrderType.PICKUP, OrderStatus.PENDING);

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(null);

            mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
