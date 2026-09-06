package com.myeshopping.deliveryservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class DeliveryRequest { @NotNull @Positive private Long shipmentId; @NotBlank private String recipientName; }
