package com.myeshopping.cartservice.repository;

import com.myeshopping.cartservice.entity.Cart;
import com.myeshopping.cartservice.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerIdAndStatus(Long customerId, CartStatus status);
}
