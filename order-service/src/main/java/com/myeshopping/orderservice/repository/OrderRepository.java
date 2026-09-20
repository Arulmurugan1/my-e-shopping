package com.myeshopping.orderservice.repository;

import com.myeshopping.orderservice.entity.Order;
import com.myeshopping.orderservice.entity.OrderStatus;
import com.myeshopping.orderservice.dto.CustomerOrderTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByStatus(OrderStatus status);

    @Query("select new com.myeshopping.orderservice.dto.CustomerOrderTotal(o.customerId, count(o), sum(o.totalAmount), max(o.createdAt)) "
            + "from Order o where o.status <> :excluded group by o.customerId order by sum(o.totalAmount) desc")
    List<CustomerOrderTotal> findCustomerTotals(@Param("excluded") OrderStatus excluded);
}
