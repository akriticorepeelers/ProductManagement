package com.ecommerce.inventoryservice.dto;

import lombok.*;

/**
 * InventoryResponse holds stock levels and availability indicators returned to clients.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private String skuCode;
    private Integer quantity;
    private Boolean inStock;
}
