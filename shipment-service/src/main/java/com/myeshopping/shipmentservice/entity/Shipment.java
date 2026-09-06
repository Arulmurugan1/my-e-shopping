package com.myeshopping.shipmentservice.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="shipments", uniqueConstraints=@UniqueConstraint(columnNames="order_id")) @Data @Builder @NoArgsConstructor @AllArgsConstructor public class Shipment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="order_id",nullable=false) private Long orderId; @Column(nullable=false,unique=true,length=60) private String trackingNumber; @Column(nullable=false,length=100) private String carrier; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ShipmentStatus status; @Column(nullable=false) private LocalDateTime createdAt; @Column private LocalDateTime completedAt;
 @PrePersist void prePersist(){createdAt=LocalDateTime.now();}
}
