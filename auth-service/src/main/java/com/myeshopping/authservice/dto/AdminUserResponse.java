package com.myeshopping.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private String id;
    private String email;
    private String username;
    private String mobileNumber;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
}
