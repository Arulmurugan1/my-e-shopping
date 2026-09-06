package com.myeshopping.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentStatusRequest {
    @NotBlank private String status;
}
