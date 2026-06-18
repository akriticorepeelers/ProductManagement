package com.ecommerce.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * OrderResponse represents order placement details returned to clients.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
    private String status;
    private String username;
}
