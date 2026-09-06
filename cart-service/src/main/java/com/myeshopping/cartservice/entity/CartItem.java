package com.myeshopping.cartservice.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false, length = 80)
    private String sku;
    @Column(nullable = false, length = 200)
    private String productName;
    @Column(nullable = false)
    private double unitPrice;
    @Column(nullable = false)
    private int quantity;
}
