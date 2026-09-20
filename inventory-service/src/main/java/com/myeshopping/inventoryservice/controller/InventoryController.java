package com.myeshopping.inventoryservice.controller;

import com.myeshopping.inventoryservice.dto.*;
import com.myeshopping.inventoryservice.entity.Product;
import com.myeshopping.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/api/v1/products")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(response("Product created successfully", inventoryService.createProduct(request)));
    }

    @GetMapping("/api/v1/products")
    public ResponseEntity<ApiResponse<List<Product>>> getProducts() {
        return ResponseEntity.ok(response("Products fetched successfully", inventoryService.getProducts()));
    }

    @GetMapping("/api/v1/products/{productId}")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(response("Product fetched successfully", inventoryService.getProduct(productId)));
    }

    @PutMapping("/api/v1/products/{productId}/image")
    public ResponseEntity<ApiResponse<Product>> updateImage(@PathVariable Long productId,
                                                             @Valid @RequestBody ProductImageRequest request) {
        return ResponseEntity.ok(response("Product image updated successfully", inventoryService.updateImage(productId, request.getImageUrl())));
    }

    @PostMapping("/api/v1/inventory/{productId}/reserve")
    public ResponseEntity<ApiResponse<Product>> reserve(@PathVariable Long productId,
                                                         @Valid @RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(response("Stock reserved successfully", inventoryService.reserveStock(productId, request)));
    }

    @PostMapping("/api/v1/inventory/{productId}/release")
    public ResponseEntity<ApiResponse<Product>> release(@PathVariable Long productId,
                                                         @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(response("Stock released successfully", inventoryService.releaseStock(productId, request)));
    }

    @PostMapping("/api/v1/inventory/{productId}/add")
    public ResponseEntity<ApiResponse<Product>> add(@PathVariable Long productId,
                                                     @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(response("Stock added successfully", inventoryService.addStock(productId, request)));
    }

    @DeleteMapping("/api/v1/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long productId) {
        inventoryService.deactivateProduct(productId);
        return ResponseEntity.ok(response("Product deactivated successfully", null));
    }

    private <T> ApiResponse<T> response(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data)
                .timestamp(Instant.now()).correlationId(UUID.randomUUID()).build();
    }
}
