package com.myeshopping.authservice.controller;

import com.myeshopping.authservice.dto.ApiResponse;
import com.myeshopping.authservice.dto.AuthResponse;
import com.myeshopping.authservice.dto.LoginRequest;
import com.myeshopping.authservice.dto.RegisterRequest;
import com.myeshopping.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Registration successful")
                .data(response)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Authentication successful")
                .data(response)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
