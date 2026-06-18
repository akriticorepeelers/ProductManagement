package com.ecommerce.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * InventoryRequest encapsulates request payloads to update stock counts.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {

    @NotBlank(message = "Sku code is required")
    private String skuCode;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be less than zero")
    private Integer quantity;
}
