package com.myeshopping.returnrefundservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ReturnRequest { @NotNull @Positive private Long orderId; @NotNull @Positive private Long customerId; @NotBlank private String reason; @Positive private double amount; }
