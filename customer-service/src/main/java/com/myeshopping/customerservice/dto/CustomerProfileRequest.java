package com.myeshopping.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerProfileRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    private String firstName;

    private String lastName;

    private String phoneNumber;
}
