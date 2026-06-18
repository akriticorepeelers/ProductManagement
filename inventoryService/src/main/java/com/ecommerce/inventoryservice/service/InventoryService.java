package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;

/**
 * InventoryService declares business contracts for stock checking, updates, and reductions.
 */
public interface InventoryService {

    /**
     * Initializes or increments the stock level for a SKU.
     */
    InventoryResponse updateStock(InventoryRequest request);

    /**
     * Retrieves the current stock level details for a SKU.
     */
    InventoryResponse getStockBySku(String skuCode);

    /**
     * Checks if the requested quantity is available in stock.
     */
    boolean checkAvailability(String skuCode, Integer quantity);

    /**
     * Decrements the stock quantity for a SKU after successful order placement.
     */
    void reduceStock(String skuCode, Integer quantity);
}
