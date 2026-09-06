package com.myeshopping.orderservice.repository;

import com.myeshopping.orderservice.entity.Order;
import com.myeshopping.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByStatus(OrderStatus status);
}
