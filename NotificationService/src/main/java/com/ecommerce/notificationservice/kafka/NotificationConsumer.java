package com.ecommerce.notificationservice.kafka;

import com.ecommerce.notificationservice.dto.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * NotificationConsumer listens to Kafka order topic events and generates logs mimicking email alerts.
 */
@Service
@Slf4j
public class NotificationConsumer {

    /**
     * Consumes incoming OrderPlacedEvent from 'order-placed-topic'.
     */
    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group")
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("### Event Received - Processing Order Notification ###");
        log.info("Received event for Order Number: {}", event.getOrderNumber());
        log.info("Product SKU: {}, Quantity: {}", event.getSkuCode(), event.getQuantity());
        log.info("Customer Username: {}, Email: {}", event.getUsername(), event.getEmail());

        // Simulate notification dispatch logic
        sendSimulatedEmailNotification(event);
    }

    private void sendSimulatedEmailNotification(OrderPlacedEvent event) {
        log.info("----------------------------------------------------------------------");
        log.info("EMAIL NOTIFICATION DISPATCHED SUCCESSFULLY");
        log.info("To: {}", event.getEmail());
        log.info("Subject: E-Commerce Order Confirmation - #{}", event.getOrderNumber());
        log.info("Dear {},", event.getUsername());
        log.info("Thank you for your order! We are pleased to confirm your purchase of:");
        log.info(" - Product SKU: {} (x{} units)", event.getSkuCode(), event.getQuantity());
        log.info("We are preparing your shipment. You will receive tracking coordinates shortly.");
        log.info("Warm regards,\nThe E-Commerce E-Store Team");
        log.info("----------------------------------------------------------------------");
    }
}
