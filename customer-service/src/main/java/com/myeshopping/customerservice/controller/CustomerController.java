package com.myeshopping.customerservice.controller;

import com.myeshopping.customerservice.dto.AddressRequest;
import com.myeshopping.customerservice.dto.ApiResponse;
import com.myeshopping.customerservice.dto.CustomerProfileRequest;
import com.myeshopping.customerservice.entity.CustomerAddress;
import com.myeshopping.customerservice.entity.CustomerProfile;
import com.myeshopping.customerservice.service.CustomerProfileDashboard;
import com.myeshopping.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfile>> createProfile(@Valid @RequestBody CustomerProfileRequest request) {
        CustomerProfile profile = customerService.createOrUpdateProfile(request);
        return ResponseEntity.ok(buildResponse("Customer profile saved successfully", profile));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<CustomerProfile>> getProfile(@PathVariable Long userId) {
        CustomerProfile profile = customerService.getProfileByUserId(userId);
        return ResponseEntity.ok(buildResponse("Customer profile fetched successfully", profile));
    }

    @PostMapping("/{userId}/addresses")
    public ResponseEntity<ApiResponse<CustomerAddress>> addAddress(@PathVariable Long userId,
                                                                @Valid @RequestBody AddressRequest request) {
        CustomerAddress address = customerService.addAddress(userId, request);
        return ResponseEntity.ok(buildResponse("Address saved successfully", address));
    }

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<ApiResponse<List<CustomerAddress>>> getAddresses(@PathVariable Long userId) {
        List<CustomerAddress> addresses = customerService.getAddresses(userId);
        return ResponseEntity.ok(buildResponse("Addresses fetched successfully", addresses));
    }

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<ApiResponse<CustomerProfileDashboard>> getDashboard(@PathVariable Long userId) {
        CustomerProfileDashboard dashboard = customerService.getDashboard(userId);
        return ResponseEntity.ok(buildResponse("Dashboard fetched successfully", dashboard));
    }

    private <T> ApiResponse<T> buildResponse(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .build();
    }
}
