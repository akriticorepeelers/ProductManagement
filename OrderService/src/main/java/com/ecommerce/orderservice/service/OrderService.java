package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;

import java.util.List;

/**
 * OrderService declares business contracts for placing, cancelling, and viewing customer orders.
 */
public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse cancelOrder(Long id);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByUsername(String username);

    List<OrderResponse> getAllOrders();
}
