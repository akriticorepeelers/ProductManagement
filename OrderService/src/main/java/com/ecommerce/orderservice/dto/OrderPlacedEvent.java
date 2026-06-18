package com.ecommerce.orderservice.dto;

import lombok.*;

/**
 * OrderPlacedEvent represents the event message published to Kafka upon successful order placement.
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
    private String email; // User's email to send confirmation notification to
}
