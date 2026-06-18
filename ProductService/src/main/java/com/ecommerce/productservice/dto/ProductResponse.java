package com.ecommerce.productservice.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * ProductResponse represents catalog details sent back to clients.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String skuCode;
}
