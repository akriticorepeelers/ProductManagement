package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.dto.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * OrderProducer manages publishing order lifecycle events to Kafka topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    /**
     * Sends OrderPlacedEvent to the 'order-placed-topic' topic.
     * Gracefully logs and catches connection errors if brokers are unavailable in testing.
     */
    public void sendOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Sending OrderPlacedEvent to Kafka. Order Number: {}", event.getOrderNumber());
        try {
            kafkaTemplate.send("order-placed-topic", event);
        } catch (Exception e) {
            log.error("Failed to publish order event to Kafka: {}. Continuing order processing.", e.getMessage());
        }
    }
}
