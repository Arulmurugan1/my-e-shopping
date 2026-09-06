package com.myeshopping.deliveryservice.dto;
import lombok.*; import java.time.Instant; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class ApiResponse<T>{private boolean success;private String message;private T data;private Instant timestamp;private UUID correlationId;}
