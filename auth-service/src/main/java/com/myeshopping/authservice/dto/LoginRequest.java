package com.myeshopping.authservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /** Username, email address or mobile number. "email" is still accepted as an alias. */
    @NotBlank(message = "Username, email or mobile number is required")
    @JsonAlias("email")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
