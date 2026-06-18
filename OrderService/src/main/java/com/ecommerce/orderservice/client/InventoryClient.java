package com.ecommerce.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * InventoryClient is an OpenFeign client to communicate with the Inventory Service.
 * Resolves the service URL dynamically via Eureka discovery lookup.
 */
@FeignClient(name = "inventory-service", path = "/api/v1/inventory")
public interface InventoryClient {

    /**
     * Feign mappings to check if stock level is sufficient.
     */
    @GetMapping("/check")
    boolean checkAvailability(@RequestParam("skuCode") String skuCode, @RequestParam("quantity") Integer quantity);

    /**
     * Feign mappings to reduce stock level for a SKU.
     */
    @PutMapping("/reduce")
    void reduceStock(@RequestParam("skuCode") String skuCode, @RequestParam("quantity") Integer quantity);
}
