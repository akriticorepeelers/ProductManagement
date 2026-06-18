package com.ecommerce.orderservice.serviceimpl;

import com.ecommerce.orderservice.client.InventoryClient;
import com.ecommerce.orderservice.dto.OrderPlacedEvent;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.kafka.OrderProducer;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OrderServiceImpl implements e-commerce order lifecycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Placing order request for SKU: {} with quantity: {}", request.getSkuCode(), request.getQuantity());

        // 1. Identify user context and email from JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String email = "";
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            email = (String) jwtAuth.getTokenAttributes().getOrDefault("email", username + "@example.com");
        }

        // 2. Query stock level availability in Inventory Service via OpenFeign
        boolean isStockAvailable = inventoryClient.checkAvailability(request.getSkuCode(), request.getQuantity());
        if (!isStockAvailable) {
            log.warn("SKU Code '{}' has insufficient stock", request.getSkuCode());
            throw new RuntimeException("Insufficient stock for product SKU: " + request.getSkuCode());
        }

        // 3. Deduct stock quantity in Inventory Service via OpenFeign
        try {
            inventoryClient.reduceStock(request.getSkuCode(), request.getQuantity());
        } catch (Exception e) {
            log.error("Stock allocation reduction failed for SKU '{}': {}", request.getSkuCode(), e.getMessage());
            throw new RuntimeException("Could not allocate stock: " + e.getMessage());
        }

        // 4. Save order to database
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .skuCode(request.getSkuCode())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .status("PLACED")
                .username(username)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved successfully with Order Number: {}", savedOrder.getOrderNumber());

        // 5. Emit Order Placed Event to Kafka broker
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderNumber(savedOrder.getOrderNumber())
                .skuCode(savedOrder.getSkuCode())
                .quantity(savedOrder.getQuantity())
                .username(savedOrder.getUsername())
                .email(email)
                .build();

        orderProducer.sendOrderPlacedEvent(event);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("Canceling order ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Order is already cancelled.");
        }

        order.setStatus("CANCELLED");
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUsername(String username) {
        log.info("Fetching orders for user: {}", username);
        return orderRepository.findByUsername(username).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .skuCode(order.getSkuCode())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .status(order.getStatus())
                .username(order.getUsername())
                .build();
    }
}
