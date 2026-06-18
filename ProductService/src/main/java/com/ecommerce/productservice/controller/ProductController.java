package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController exposes APIs to perform CRUD on the product catalog.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Adds a new product to the catalog. Requires ROLE_ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing catalog product. Requires ROLE_ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a catalog product. Requires ROLE_ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a catalog product by id. Open to ROLE_ADMIN and ROLE_CUSTOMER.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all catalog products. Open to ROLE_ADMIN and ROLE_CUSTOMER.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    /**
     * Keyword search on product name or description. Open to ROLE_ADMIN and ROLE_CUSTOMER.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String query) {
        List<ProductResponse> response = productService.searchProducts(query);
        return ResponseEntity.ok(response);
    }
}
