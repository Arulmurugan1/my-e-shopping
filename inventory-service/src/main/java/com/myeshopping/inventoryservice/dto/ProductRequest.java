package com.myeshopping.inventoryservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank private String sku;
    @NotBlank private String name;
    private String description;
    @PositiveOrZero private double price;
    @PositiveOrZero private int initialStock;
}
