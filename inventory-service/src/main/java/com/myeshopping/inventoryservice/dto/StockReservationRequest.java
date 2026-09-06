package com.myeshopping.inventoryservice.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockReservationRequest {
    @Positive private int quantity;
}
