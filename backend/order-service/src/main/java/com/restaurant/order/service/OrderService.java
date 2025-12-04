package com.restaurant.order.service;

import com.restaurant.order.dto.CreateOrderRequest;
import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.dto.UpdateStatusRequest;
import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.OrderItem;
import com.restaurant.order.entity.OrderStatus;
import com.restaurant.order.exception.InvalidStatusTransitionException;
import com.restaurant.order.exception.OrderNotFoundException;
import com.restaurant.order.kafka.OrderEvent;
import com.restaurant.order.kafka.OrderEventProducer;
import com.restaurant.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order Service - Business Logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    /**
     * Create new order
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Generate order number
        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .customerId(request.getCustomerId())
                .deliveryStreet(request.getDeliveryAddress() != null ? request.getDeliveryAddress().getStreet() : null)
                .deliveryCity(request.getDeliveryAddress() != null ? request.getDeliveryAddress().getCity() : null)
                .deliveryPostalCode(request.getDeliveryAddress() != null ? request.getDeliveryAddress().getPostalCode() : null)
                .orderType(request.getOrderType())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .status(OrderStatus.PENDING)
                .totalPrice(request.getTotalPrice())
                .estimatedDelivery(LocalDateTime.now().plusMinutes(45))
                .build();

        // Add items
        if (request.getItems() != null) {
            for (var itemRequest : request.getItems()) {
                OrderItem item = OrderItem.builder()
                        .productId(itemRequest.getProductId())
                        .productName(itemRequest.getProductName())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(itemRequest.getUnitPrice())
                        .totalPrice(itemRequest.getTotalPrice())
                        .build();
                order.addItem(item);
            }
        }

        Order saved = orderRepository.save(order);
        log.info("Created order: {}", orderNumber);

        // Send Kafka event
        eventProducer.sendOrderCreated(buildEvent(saved));

        return OrderResponse.fromEntity(saved);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Get all orders with optional filters
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(OrderStatus status, LocalDate date) {
        List<Order> orders;

        if (status != null && date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            orders = orderRepository.findByStatusAndCreatedAtBetween(status, start, end);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            orders = orderRepository.findByCreatedAtBetween(start, end);
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update order status
     */
    public OrderResponse updateStatus(Long id, UpdateStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        OrderStatus newStatus = request.getStatus();

        // Validate status transition
        if (!isValidTransition(order.getStatus(), newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        log.info("Updated order {} status to {}", order.getOrderNumber(), newStatus);

        // Send Kafka event
        if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.PICKED_UP) {
            eventProducer.sendOrderCompleted(buildEvent(saved));
        } else {
            eventProducer.sendOrderUpdated(buildEvent(saved));
        }

        return OrderResponse.fromEntity(saved);
    }

    /**
     * Get orders by customer ID
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        String datePart = LocalDate.now().toString().replace("-", "");
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }

    /**
     * Validate status transition
     */
    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.IN_PROGRESS || next == OrderStatus.CANCELLED;
            case IN_PROGRESS -> next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY -> next == OrderStatus.DELIVERED || next == OrderStatus.PICKED_UP;
            case DELIVERED, PICKED_UP, CANCELLED -> false;
        };
    }

    /**
     * Build Kafka event from order
     */
    private OrderEvent buildEvent(Order order) {
        return OrderEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .orderType(order.getOrderType().name())
                .customerName(order.getCustomerName())
                .totalPrice(order.getTotalPrice())
                .itemCount(order.getItems().size())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
