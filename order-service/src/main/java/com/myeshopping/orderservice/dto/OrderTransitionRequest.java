package com.myeshopping.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderTransitionRequest {
    @NotBlank private String status;
}
