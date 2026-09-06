package com.myeshopping.pickingservice.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="picking_tasks") @Data @Builder @NoArgsConstructor @AllArgsConstructor public class PickingTask {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private Long orderId; @Column(nullable=false) private int itemCount; @Enumerated(EnumType.STRING) @Column(nullable=false) private PickingStatus status; @Column(nullable=false) private LocalDateTime createdAt; @Column(nullable=false) private LocalDateTime updatedAt;
 @PrePersist void prePersist(){createdAt=LocalDateTime.now();updatedAt=createdAt;} @PreUpdate void preUpdate(){updatedAt=LocalDateTime.now();}
}
