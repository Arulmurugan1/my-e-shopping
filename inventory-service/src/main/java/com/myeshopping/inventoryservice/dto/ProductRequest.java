package com.myeshopping.inventoryservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank private String sku;
    @NotBlank private String name;
    private String description;
    @Size(max = 1000) @Pattern(regexp = "^(https?://\\S+)?$", message = "Image URL must start with http:// or https://")
    private String imageUrl;
    @PositiveOrZero private double price;
    @PositiveOrZero private int initialStock;
}
