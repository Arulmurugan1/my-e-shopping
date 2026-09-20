package com.myeshopping.authservice.dto;

import lombok.Data;

@Data
public class AdminUpdateRequest {
    private Boolean active;
    private String role;
}
