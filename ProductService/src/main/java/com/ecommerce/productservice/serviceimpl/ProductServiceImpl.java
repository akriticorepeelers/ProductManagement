package com.ecommerce.productservice.serviceimpl;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductServiceImpl implements business catalog operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSkuCode());
        if (productRepository.findBySkuCode(request.getSkuCode()).isPresent()) {
            throw new RuntimeException("Product SKU code already exists");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .skuCode(request.getSkuCode())
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        // SKU conflict check
        if (!product.getSkuCode().equals(request.getSkuCode()) && 
            productRepository.findBySkuCode(request.getSkuCode()).isPresent()) {
            throw new RuntimeException("Product SKU code already exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSkuCode(request.getSkuCode());

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        return mapToProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query) {
        log.info("Searching products with query: {}", query);
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .skuCode(product.getSkuCode())
                .build();
    }
}
