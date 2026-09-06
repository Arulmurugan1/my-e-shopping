package com.myeshopping.cartservice.service;

import com.myeshopping.cartservice.dto.AddCartItemRequest;
import com.myeshopping.cartservice.dto.CheckoutValidation;
import com.myeshopping.cartservice.entity.Cart;
import com.myeshopping.cartservice.entity.CartItem;
import com.myeshopping.cartservice.entity.CartStatus;
import com.myeshopping.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    @Transactional
    public Cart getOrCreateActiveCart(Long customerId) {
        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(Cart.builder().customerId(customerId).status(CartStatus.ACTIVE).build()));
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long customerId) {
        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Active cart not found for customer: " + customerId));
    }

    @Transactional
    public Cart addItem(Long customerId, AddCartItemRequest request) {
        Cart cart = getOrCreateActiveCart(customerId);
        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProductId().equals(request.getProductId()))
                .findFirst().orElse(null);
        if (item == null) {
            item = CartItem.builder().cart(cart).productId(request.getProductId()).sku(request.getSku())
                    .productName(request.getProductName()).unitPrice(request.getUnitPrice()).quantity(request.getQuantity()).build();
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setUnitPrice(request.getUnitPrice());
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItem(Long customerId, Long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Cart cart = getCart(customerId);
        CartItem item = findItem(cart, productId);
        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Long customerId, Long productId) {
        Cart cart = getCart(customerId);
        CartItem item = findItem(cart, productId);
        cart.getItems().remove(item);
        return cartRepository.save(cart);
    }

    @Transactional
    public CheckoutValidation validateCheckout(Long customerId) {
        Cart cart = getCart(customerId);
        if (cart.getItems().isEmpty()) {
            return CheckoutValidation.builder().valid(false).message("Cart cannot be checked out when empty")
                    .totalItems(0).totalAmount(0).build();
        }
        return CheckoutValidation.builder().valid(true).message("Cart is ready for checkout")
                .totalItems(cart.getTotalItems()).totalAmount(cart.getTotalAmount()).build();
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream().filter(item -> item.getProductId().equals(productId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product is not in the cart: " + productId));
    }
}
