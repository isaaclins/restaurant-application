package com.restaurant.order.dto;

import com.restaurant.order.entity.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String customerEmail;
    private String customerPhone;
    private Long customerId;

    private AddressDTO deliveryAddress;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    private String paymentMethod;
    private String notes;

    private List<OrderItemRequest> items;

    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;

    // Optional: If not provided, defaults to now + 45 minutes
    private LocalDateTime estimatedDelivery;
}
