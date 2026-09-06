package com.myeshopping.paymentservice.controller;

import com.myeshopping.paymentservice.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handle(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder().success(false).message(exception.getMessage()).timestamp(Instant.now()).correlationId(UUID.randomUUID()).build());
    }
}
