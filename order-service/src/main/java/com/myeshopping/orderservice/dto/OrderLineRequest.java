package com.myeshopping.orderservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderLineRequest {
    
    @NotNull 
    @Positive 
    private Long productId;

    @NotBlank 
    private String sku;

    @NotBlank 
    private String productName;

    @PositiveOrZero 
    private double unitPrice;

    @Positive 
    private int quantity;
    
}
