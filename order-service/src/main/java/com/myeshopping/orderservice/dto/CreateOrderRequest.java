package com.myeshopping.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    
    @NotNull 
    @Positive 
    private Long customerId;
    
    @NotBlank 
    private String shippingAddress;
    
    @NotEmpty 
    @Valid 
    private List<OrderLineRequest> lines;
    
}
