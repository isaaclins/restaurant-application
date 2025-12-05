package com.restaurant.order.config;

import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.OrderItem;
import com.restaurant.order.entity.OrderStatus;
import com.restaurant.order.entity.OrderType;
import com.restaurant.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Loads demo orders for development and testing.
 * Creates sample orders if none exist.
 * 
 * DISABLED: Demo data can be created via Developer Settings in the UI.
 * To re-enable, change profile to @Profile("demo")
 */
// @Component // Disabled - use Developer Settings > Populate Demo Data instead
@RequiredArgsConstructor
@Slf4j
@Profile("demo") // Only runs with 'demo' profile (disabled by default)
public class DataLoader implements CommandLineRunner {

        private final OrderRepository orderRepository;

        @Override
        public void run(String... args) {
                if (orderRepository.count() == 0) {
                        createDemoOrders();
                } else {
                        log.info("Orders already exist, skipping demo data creation");
                }
        }

        private void createDemoOrders() {
                log.info("Creating demo orders for KDS testing...");

                // Pickup Order 1 - Ready soon
                createOrder(
                                "Max Müller",
                                "max@example.com",
                                "+41 79 123 4567",
                                OrderType.PICKUP,
                                OrderStatus.CONFIRMED,
                                LocalDateTime.now().plusMinutes(5),
                                new String[] { "Margherita Pizza", "Coca Cola" },
                                new int[] { 1, 2 },
                                new double[] { 18.50, 4.50 });

                // Pickup Order 2 - New
                createOrder(
                                "Anna Schmidt",
                                "anna@example.com",
                                "+41 78 234 5678",
                                OrderType.PICKUP,
                                OrderStatus.PENDING,
                                LocalDateTime.now().plusMinutes(15),
                                new String[] { "Quattro Formaggi", "Tiramisu" },
                                new int[] { 1, 1 },
                                new double[] { 22.00, 8.50 });

                // Delivery Order 1
                Order deliveryOrder1 = createOrder(
                                "Peter Meier",
                                "peter@example.com",
                                "+41 76 345 6789",
                                OrderType.DELIVERY,
                                OrderStatus.IN_PROGRESS,
                                LocalDateTime.now().plusMinutes(25),
                                new String[] { "Diavola Pizza", "Bruschetta", "Fanta" },
                                new int[] { 2, 1, 2 },
                                new double[] { 21.00, 9.50, 4.50 });
                deliveryOrder1.setDeliveryStreet("Bahnhofstrasse 42");
                deliveryOrder1.setDeliveryCity("Zürich");
                deliveryOrder1.setDeliveryPostalCode("8001");
                orderRepository.save(deliveryOrder1);

                // Delivery Order 2
                Order deliveryOrder2 = createOrder(
                                "Sarah Weber",
                                "sarah@example.com",
                                "+41 77 456 7890",
                                OrderType.DELIVERY,
                                OrderStatus.CONFIRMED,
                                LocalDateTime.now().plusMinutes(20),
                                new String[] { "Calzone", "Caesar Salad" },
                                new int[] { 1, 1 },
                                new double[] { 19.50, 14.00 });
                deliveryOrder2.setDeliveryStreet("Limmatquai 15");
                deliveryOrder2.setDeliveryCity("Zürich");
                deliveryOrder2.setDeliveryPostalCode("8001");
                orderRepository.save(deliveryOrder2);

                // Delivery Order 3 - Urgent (almost due)
                Order deliveryOrder3 = createOrder(
                                "Thomas Keller",
                                "thomas@example.com",
                                "+41 79 567 8901",
                                OrderType.DELIVERY,
                                OrderStatus.IN_PROGRESS,
                                LocalDateTime.now().plusMinutes(3),
                                new String[] { "Pasta Carbonara", "Garlic Bread" },
                                new int[] { 1, 1 },
                                new double[] { 17.50, 6.00 });
                deliveryOrder3.setDeliveryStreet("Niederdorfstrasse 8");
                deliveryOrder3.setDeliveryCity("Zürich");
                deliveryOrder3.setDeliveryPostalCode("8001");
                orderRepository.save(deliveryOrder3);

                // Pickup Order 3 - Overdue
                createOrder(
                                "Lisa Brunner",
                                "lisa@example.com",
                                "+41 78 678 9012",
                                OrderType.PICKUP,
                                OrderStatus.READY,
                                LocalDateTime.now().minusMinutes(5),
                                new String[] { "Insalata Mista", "Minestrone" },
                                new int[] { 1, 1 },
                                new double[] { 12.00, 9.00 });

                // Delivery Order 4
                Order deliveryOrder4 = createOrder(
                                "Michael Huber",
                                "michael@example.com",
                                "+41 76 789 0123",
                                OrderType.DELIVERY,
                                OrderStatus.PENDING,
                                LocalDateTime.now().plusMinutes(30),
                                new String[] { "Prosciutto Pizza", "Caprese", "Espresso" },
                                new int[] { 1, 1, 2 },
                                new double[] { 23.00, 11.50, 4.00 });
                deliveryOrder4.setDeliveryStreet("Uraniastrasse 20");
                deliveryOrder4.setDeliveryCity("Zürich");
                deliveryOrder4.setDeliveryPostalCode("8001");
                orderRepository.save(deliveryOrder4);

                log.info("Created 6 demo orders for KDS");
        }

        private Order createOrder(
                        String customerName,
                        String customerEmail,
                        String customerPhone,
                        OrderType orderType,
                        OrderStatus status,
                        LocalDateTime estimatedDelivery,
                        String[] productNames,
                        int[] quantities,
                        double[] prices) {
                String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                Order order = Order.builder()
                                .orderNumber(orderNumber)
                                .customerName(customerName)
                                .customerEmail(customerEmail)
                                .customerPhone(customerPhone)
                                .orderType(orderType)
                                .status(status)
                                .estimatedDelivery(estimatedDelivery)
                                .createdAt(LocalDateTime.now().minusMinutes((int) (Math.random() * 30)))
                                .totalPrice(BigDecimal.ZERO)
                                .build();

                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < productNames.length; i++) {
                        BigDecimal unitPrice = BigDecimal.valueOf(prices[i]);
                        BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantities[i]));

                        OrderItem item = OrderItem.builder()
                                        .productId((long) (i + 1))
                                        .productName(productNames[i])
                                        .quantity(quantities[i])
                                        .unitPrice(unitPrice)
                                        .totalPrice(itemTotal)
                                        .build();

                        order.addItem(item);
                        total = total.add(itemTotal);
                }

                order.setTotalPrice(total);
                return orderRepository.save(order);
        }
}
