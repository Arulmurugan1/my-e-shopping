package com.myeshopping.paymentservice.controller;

import com.myeshopping.paymentservice.dto.*;
import com.myeshopping.paymentservice.entity.Payment;
import com.myeshopping.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> initiate(@RequestHeader(value = "Idempotency-Key", required = false) String key, @Valid @RequestBody PaymentRequest request) { return ok("Payment initiated successfully", paymentService.initiate(request, key)); }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> get(@PathVariable Long id) { return ok("Payment fetched successfully", paymentService.get(id)); }
    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Payment>> status(@PathVariable Long id, @Valid @RequestBody PaymentStatusRequest request) { return ok("Payment status updated successfully", paymentService.transition(id, request.getStatus())); }
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<Payment>> refund(@PathVariable Long id) { return ok("Refund requested successfully", paymentService.requestRefund(id)); }
    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) { return ResponseEntity.ok(ApiResponse.<T>builder().success(true).message(message).data(data).timestamp(Instant.now()).correlationId(UUID.randomUUID()).build()); }
}
