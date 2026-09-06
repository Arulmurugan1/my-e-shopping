package com.myeshopping.cartservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull @Positive private Long productId;
    @NotBlank private String sku;
    @NotBlank private String productName;
    @PositiveOrZero private double unitPrice;
    @Positive private int quantity;
}
