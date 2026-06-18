package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;

import java.util.List;

/**
 * ProductService declares catalog CRUD and keyword search business contracts.
 */
public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> searchProducts(String query);
}
