package com.myeshopping.inventoryservice.controller;

import com.myeshopping.inventoryservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessError(IllegalArgumentException exception) {
        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).message(exception.getMessage())
                .timestamp(Instant.now()).correlationId(UUID.randomUUID()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
