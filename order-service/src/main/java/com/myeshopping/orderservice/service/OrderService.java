package com.myeshopping.orderservice.service;

import com.myeshopping.orderservice.client.InventoryClient;
import com.myeshopping.orderservice.dto.*;
import com.myeshopping.orderservice.entity.*;
import com.myeshopping.orderservice.repository.OrderRepository;
import com.myeshopping.orderservice.event.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectProvider<OrderEventPublisher> orderEventPublisher;
    private final InventoryClient inventoryClient;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.ORDERED, Set.of(OrderStatus.PICKING_PENDING, OrderStatus.CANCELLED),
            OrderStatus.PICKING_PENDING, Set.of(OrderStatus.PICKING_IN_PROGRESS, OrderStatus.CANCELLED),
            OrderStatus.PICKING_IN_PROGRESS, Set.of(OrderStatus.PICKED),
            OrderStatus.PICKED, Set.of(OrderStatus.SHIPPING_PENDING),
            OrderStatus.SHIPPING_PENDING, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.IN_DELIVERY),
            OrderStatus.IN_DELIVERY, Set.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED));

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        Order order = Order.builder()
                            .customerId(request.getCustomerId())
                            .shippingAddress(request.getShippingAddress())
                            .status(OrderStatus.ORDERED).build();
        
        double total = 0;
        
        for (OrderLineRequest lineRequest : request.getLines()) {
            
            OrderLine line = OrderLine
                                .builder()
                                .order(order)
                                .productId(lineRequest.getProductId())
                                .sku(lineRequest.getSku())
                                .productName(lineRequest.getProductName())
                                .unitPrice(lineRequest.getUnitPrice())
                                .quantity(lineRequest.getQuantity())
                                .build();

            order.getLines().add(line);
            
            total += lineRequest.getUnitPrice() * lineRequest.getQuantity();
        }

        order.setTotalAmount(total);

        List<OrderLineRequest> reserved = new ArrayList<>();
        try {
            for (OrderLineRequest lineRequest : request.getLines()) {
                inventoryClient.reserve(lineRequest.getProductId(), lineRequest.getProductName(), lineRequest.getQuantity());
                reserved.add(lineRequest);
            }
        } catch (RuntimeException ex) {
            releaseQuietly(reserved);
            throw ex;
        }

        Order savedOrder;
        try {
            savedOrder = orderRepository.save(order);
        } catch (RuntimeException ex) {
            releaseQuietly(reserved);
            throw ex;
        }

        log.info("Order {} created for customer {}: {} line(s), total {}",
                savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getLines().size(), savedOrder.getTotalAmount());

        OrderEventPublisher publisher = orderEventPublisher.getIfAvailable();

        if (publisher != null) {
            publisher.publishCreated(savedOrder);
            log.info("Order {} created event published", savedOrder.getId());
        }

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderTotal> getCustomerTotals() {
        return orderRepository.findCustomerTotals(OrderStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id)); 
    }

    @Transactional(readOnly = true)
    public List<Order> getCustomerOrders(Long customerId) { 
        return orderRepository
                        .findByCustomerIdOrderByCreatedAtDesc(customerId); 
    }

    @Transactional
    public Order transition(Long id, String targetStatus) {
    
        Order order = getOrder(id);
        OrderStatus target;
    
        try { 
            target = OrderStatus.valueOf(targetStatus); 
        }
        catch (IllegalArgumentException exception) { 
            throw new IllegalArgumentException("Unknown order status: " + targetStatus); 
        }

        if (!TRANSITIONS.getOrDefault(order.getStatus(), Set.of()).contains(target)) {
            throw new IllegalArgumentException("Invalid order transition from " + order.getStatus() + " to " + target);
        }
        
        if (target == OrderStatus.CANCELLED) {
            for (OrderLine line : order.getLines()) {
                inventoryClient.release(line.getProductId(), line.getQuantity());
            }
        }

        order.setStatus(target);
        return orderRepository.save(order);
    }

    private void releaseQuietly(List<OrderLineRequest> reservedLines) {
        for (OrderLineRequest line : reservedLines) {
            try {
                inventoryClient.release(line.getProductId(), line.getQuantity());
            } catch (RuntimeException ex) {
                log.error("Could not roll back reserved stock for product {}: {}", line.getProductId(), ex.getMessage());
            }
        }
    }
}
