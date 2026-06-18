package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController exposes APIs to create, cancel and query customer orders.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Places a new order. Open to ROLE_CUSTOMER and ROLE_ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Cancels an existing order. Open to ROLE_CUSTOMER and ROLE_ADMIN.
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        OrderResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves an order by ID. Open to ROLE_CUSTOMER and ROLE_ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all orders placed by the current authenticated user context.
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OrderResponse> response = orderService.getOrdersByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all orders placed in the system. Requires ROLE_ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> response = orderService.getAllOrders();
        return ResponseEntity.ok(response);
    }
}
