package com.myeshopping.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_lines")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id", nullable = false) @JsonIgnore private Order order;
    @Column(nullable = false) private Long productId;
    @Column(nullable = false, length = 80) private String sku;
    @Column(nullable = false, length = 200) private String productName;
    @Column(nullable = false) private double unitPrice;
    @Column(nullable = false) private int quantity;
}
