package com.myeshopping.inventoryservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductImageRequest {
    @Size(max = 1000) @Pattern(regexp = "^(https?://\\S+)?$", message = "Image URL must start with http:// or https://")
    private String imageUrl;
}
