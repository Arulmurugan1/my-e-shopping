package com.myeshopping.paymentservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull @Positive private Long orderId;
    @NotNull @Positive private Long customerId;
    @Positive private double amount;
    @NotBlank @Size(min = 3, max = 3) private String currency;
}
