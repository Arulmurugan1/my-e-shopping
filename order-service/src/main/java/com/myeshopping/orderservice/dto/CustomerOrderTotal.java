package com.myeshopping.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderTotal {
    private Long customerId;
    private Long orderCount;
    private Double totalAmount;
    private LocalDateTime lastOrderAt;
}
