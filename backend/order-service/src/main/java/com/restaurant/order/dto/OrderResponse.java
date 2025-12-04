package com.restaurant.order.dto;

import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.OrderStatus;
import com.restaurant.order.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private AddressDTO deliveryAddress;
    private OrderType orderType;
    private OrderStatus status;
    private String paymentMethod;
    private String notes;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime estimatedDelivery;

    public static OrderResponse fromEntity(Order order) {
        AddressDTO address = null;
        if (order.getDeliveryStreet() != null) {
            address = new AddressDTO(
                order.getDeliveryStreet(),
                order.getDeliveryCity(),
                order.getDeliveryPostalCode()
            );
        }

        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(address)
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .items(items)
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .estimatedDelivery(order.getEstimatedDelivery())
                .build();
    }
}
