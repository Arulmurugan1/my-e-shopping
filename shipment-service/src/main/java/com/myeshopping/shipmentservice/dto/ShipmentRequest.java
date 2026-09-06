package com.myeshopping.shipmentservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ShipmentRequest { @NotNull @Positive private Long orderId; @NotBlank private String carrier; }
