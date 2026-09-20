package com.myeshopping.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])[A-Za-z0-9_.]{3,30}$",
            message = "Username must be 3-30 characters (letters, digits, _ or .) and contain a letter")
    private String username;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9 ()\\-]{7,20}$", message = "Mobile number must be 7-15 digits, optionally starting with +")
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain letters and numbers")
    private String password;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in the format YYYY-MM-DD")
    private String dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "(?i)^(MALE|FEMALE|OTHER)$", message = "Gender must be male, female or other")
    private String gender;
}
