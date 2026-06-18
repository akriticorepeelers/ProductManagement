package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Inventory entity representing product stock levels inside MySQL inventorydb.
 */
@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    @Column(nullable = false)
    private Integer quantity;
}
