package com.myeshopping.cartservice.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CheckoutValidation {
    private boolean valid;
    private String message;
    private int totalItems;
    private double totalAmount;
}
