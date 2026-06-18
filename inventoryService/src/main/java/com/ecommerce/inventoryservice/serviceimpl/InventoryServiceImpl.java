package com.ecommerce.inventoryservice.serviceimpl;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InventoryServiceImpl manages database stocks level transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        log.info("Updating stock level for SKU: {} by {}", request.getSkuCode(), request.getQuantity());
        Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                .orElse(Inventory.builder()
                        .skuCode(request.getSkuCode())
                        .quantity(0)
                        .build());

        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToInventoryResponse(savedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getStockBySku(String skuCode) {
        log.info("Fetching stock details for SKU: {}", skuCode);
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Inventory details not found for SKU: " + skuCode));
        return mapToInventoryResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkAvailability(String skuCode, Integer quantity) {
        log.info("Checking availability for SKU: {}, quantity requested: {}", skuCode, quantity);
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory -> inventory.getQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public void reduceStock(String skuCode, Integer quantity) {
        log.info("Reducing stock for SKU: {} by {}", skuCode, quantity);
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product SKU code " + skuCode + " not found in inventory"));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product SKU: " + skuCode + 
                    ". Available: " + inventory.getQuantity() + ", Requested: " + quantity);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
        log.info("Successfully reduced stock for SKU: {}. New quantity: {}", skuCode, inventory.getQuantity());
    }

    private InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .skuCode(inventory.getSkuCode())
                .quantity(inventory.getQuantity())
                .inStock(inventory.getQuantity() > 0)
                .build();
    }
}
