package com.myeshopping.cartservice.controller;

import com.myeshopping.cartservice.dto.*;
import com.myeshopping.cartservice.entity.Cart;
import com.myeshopping.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Cart>> getCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(response("Cart fetched successfully", cartService.getCart(customerId)));
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<ApiResponse<Cart>> addItem(@PathVariable Long customerId,
                                                      @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(response("Item added successfully", cartService.addItem(customerId, request)));
    }

    @PutMapping("/{customerId}/items/{productId}")
    public ResponseEntity<ApiResponse<Cart>> updateItem(@PathVariable Long customerId, @PathVariable Long productId,
                                                         @RequestParam int quantity) {
        return ResponseEntity.ok(response("Item updated successfully", cartService.updateItem(customerId, productId, quantity)));
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    public ResponseEntity<ApiResponse<Cart>> removeItem(@PathVariable Long customerId, @PathVariable Long productId) {
        return ResponseEntity.ok(response("Item removed successfully", cartService.removeItem(customerId, productId)));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Cart>> clearCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(response("Cart cleared successfully", cartService.clearCart(customerId)));
    }

    @PostMapping("/{customerId}/checkout")
    public ResponseEntity<ApiResponse<CheckoutValidation>> checkout(@PathVariable Long customerId) {
        return ResponseEntity.ok(response("Checkout validation completed", cartService.validateCheckout(customerId)));
    }

    private <T> ApiResponse<T> response(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data)
                .timestamp(Instant.now()).correlationId(UUID.randomUUID()).build();
    }
}
