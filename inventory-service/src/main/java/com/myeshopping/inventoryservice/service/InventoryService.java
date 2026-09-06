package com.myeshopping.inventoryservice.service;

import com.myeshopping.inventoryservice.dto.ProductRequest;
import com.myeshopping.inventoryservice.dto.StockAdjustmentRequest;
import com.myeshopping.inventoryservice.dto.StockReservationRequest;
import com.myeshopping.inventoryservice.entity.Product;
import com.myeshopping.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductRepository productRepository;

    @Transactional
    public Product createProduct(ProductRequest request) {
        if (productRepository.findBySkuAndActiveTrue(request.getSku()).isPresent()) {
            throw new IllegalArgumentException("Active product already exists for SKU: " + request.getSku());
        }
        Product product = Product.builder()
                .sku(request.getSku()).name(request.getName()).description(request.getDescription())
                .price(request.getPrice()).stockQuantity(request.getInitialStock())
                .reservedQuantity(0).active(true).build();
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active product not found: " + productId));
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() { return productRepository.findByActiveTrue(); }

    @Transactional
    public Product reserveStock(Long productId, StockReservationRequest request) {
        Product product = getProduct(productId);
        if (request.getQuantity() > product.getAvailableQuantity()) {
            throw new IllegalArgumentException("Insufficient available stock for product: " + productId);
        }
        product.setReservedQuantity(product.getReservedQuantity() + request.getQuantity());
        return productRepository.save(product);
    }

    @Transactional
    public Product releaseStock(Long productId, StockAdjustmentRequest request) {
        Product product = getProduct(productId);
        if (request.getQuantity() > product.getReservedQuantity()) {
            throw new IllegalArgumentException("Cannot release more stock than reserved for product: " + productId);
        }
        product.setReservedQuantity(product.getReservedQuantity() - request.getQuantity());
        return productRepository.save(product);
    }

    @Transactional
    public Product addStock(Long productId, StockAdjustmentRequest request) {
        Product product = getProduct(productId);
        product.setStockQuantity(product.getStockQuantity() + request.getQuantity());
        return productRepository.save(product);
    }

    @Transactional
    public void deactivateProduct(Long productId) {
        Product product = getProduct(productId);
        product.setActive(false);
        productRepository.save(product);
    }
}
