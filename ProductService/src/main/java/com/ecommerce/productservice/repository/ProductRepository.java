package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository provides DB operations for the Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuCode(String skuCode);

    /**
     * Searches products containing the query string in name or description (case-insensitive).
     */
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
