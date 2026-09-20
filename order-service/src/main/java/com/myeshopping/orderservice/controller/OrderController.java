package com.myeshopping.orderservice.controller;

import com.myeshopping.orderservice.dto.*;
import com.myeshopping.orderservice.entity.Order;
import com.myeshopping.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity < ApiResponse < Order >> create(@Valid @RequestBody CreateOrderRequest request) {
    return ok("Order created successfully", orderService.createOrder(request));
  }

  @GetMapping("/{id}")
  public ResponseEntity < ApiResponse < Order >> get(@PathVariable Long id) {
    return ok("Order fetched successfully", orderService.getOrder(id));
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity < ApiResponse < List < Order >>> customerOrders(@PathVariable Long customerId) {
    return ok("Orders fetched successfully", orderService.getCustomerOrders(customerId));
  }

  @GetMapping("/admin/customer-totals")
  public ResponseEntity < ApiResponse < List < CustomerOrderTotal >>> customerTotals() {
    return ok("Customer order totals fetched successfully", orderService.getCustomerTotals());
  }

  @PostMapping("/{id}/transition")
  public ResponseEntity < ApiResponse < Order >> transition(@PathVariable Long id, @Valid @RequestBody OrderTransitionRequest request) {
    return ok("Order transitioned successfully", orderService.transition(id, request.getStatus()));
  }

  private < T > ResponseEntity < ApiResponse < T >> ok(String message, T data) {
    return ResponseEntity
                    .ok(ApiResponse. < T > builder()
                                    .success(true)
                                    .message(message)
                                    .data(data)
                                    .timestamp(Instant.now())
                                    .correlationId(UUID.randomUUID())
                                    .build()
                        );
  }
}