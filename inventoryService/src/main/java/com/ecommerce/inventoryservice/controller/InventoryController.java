package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * InventoryController exposes APIs to manage and inspect stocks.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Initializes or increments product stock. Requires ROLE_ADMIN.
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> updateStock(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.updateStock(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves current stock details for a SKU. Open to ROLE_ADMIN and ROLE_CUSTOMER.
     */
    @GetMapping("/{skuCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<InventoryResponse> getStockBySku(@PathVariable String skuCode) {
        InventoryResponse response = inventoryService.getStockBySku(skuCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies if requested quantity is available in stock. Open to ROLE_ADMIN and ROLE_CUSTOMER.
     */
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Boolean> checkAvailability(@RequestParam String skuCode, @RequestParam Integer quantity) {
        boolean available = inventoryService.checkAvailability(skuCode, quantity);
        return ResponseEntity.ok(available);
    }

    /**
     * Decrements stock quantity after successful order placement. Open to ROLE_ADMIN and ROLE_CUSTOMER.
     */
    @PutMapping("/reduce")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Void> reduceStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        inventoryService.reduceStock(skuCode, quantity);
        return ResponseEntity.ok().build();
    }
}
