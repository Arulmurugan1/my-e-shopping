package com.myeshopping.inventoryservice.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockAdjustmentRequest {
    @Positive private int quantity;
}
