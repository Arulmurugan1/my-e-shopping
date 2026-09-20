package com.myeshopping.orderservice.controller;

import com.myeshopping.orderservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handle(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnavailable(IllegalStateException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity
                        .status(status)
                        .body(ApiResponse
                                    .<Void>builder()
                                    .success(false)
                                    .message(message)
                                    .timestamp(Instant.now())
                                    .correlationId(UUID.randomUUID())
                                    .build()
                            );
    }
}
