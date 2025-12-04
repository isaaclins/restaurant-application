package com.restaurant.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer for Order Events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_ORDER_CREATED = "order.created";
    private static final String TOPIC_ORDER_UPDATED = "order.updated";
    private static final String TOPIC_ORDER_COMPLETED = "order.completed";

    public void sendOrderCreated(OrderEvent event) {
        log.info("Sending order.created event: {}", event.getOrderNumber());
        kafkaTemplate.send(TOPIC_ORDER_CREATED, event.getOrderNumber(), event);
    }

    public void sendOrderUpdated(OrderEvent event) {
        log.info("Sending order.updated event: {}", event.getOrderNumber());
        kafkaTemplate.send(TOPIC_ORDER_UPDATED, event.getOrderNumber(), event);
    }

    public void sendOrderCompleted(OrderEvent event) {
        log.info("Sending order.completed event: {}", event.getOrderNumber());
        kafkaTemplate.send(TOPIC_ORDER_COMPLETED, event.getOrderNumber(), event);
    }
}
