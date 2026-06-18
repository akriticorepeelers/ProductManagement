package com.ecommerce.notificationservice.dto;

import lombok.*;

/**
 * OrderPlacedEvent represents the event payload deserialized from the Kafka topic.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlacedEvent {
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String username;
    private String email;
}
