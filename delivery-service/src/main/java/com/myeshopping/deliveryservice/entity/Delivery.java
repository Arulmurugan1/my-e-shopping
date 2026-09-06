package com.myeshopping.deliveryservice.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="deliveries",uniqueConstraints=@UniqueConstraint(columnNames="shipment_id")) @Data @Builder @NoArgsConstructor @AllArgsConstructor public class Delivery {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="shipment_id",nullable=false) private Long shipmentId; @Column(nullable=false,length=160) private String recipientName; @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private DeliveryStatus status; @Column(nullable=false) private LocalDateTime createdAt; @Column private LocalDateTime deliveredAt;
 @PrePersist void prePersist(){createdAt=LocalDateTime.now();}
}
