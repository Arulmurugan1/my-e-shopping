package com.myeshopping.authservice.controller;

import com.myeshopping.authservice.dto.AdminUpdateRequest;
import com.myeshopping.authservice.dto.AdminUserResponse;
import com.myeshopping.authservice.dto.ApiResponse;
import com.myeshopping.authservice.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> list() {
        return ResponseEntity.ok(response("Users fetched successfully", adminUserService.listUsers()));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<AdminUserResponse>> setActive(@PathVariable UUID id,
                                                                     @RequestBody AdminUpdateRequest request,
                                                                     Authentication authentication) {
        if (request.getActive() == null) {
            throw new IllegalArgumentException("'active' is required");
        }
        return ResponseEntity.ok(response("User status updated", adminUserService.setActive(id, request.getActive(), authentication.getName())));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> setRole(@PathVariable UUID id,
                                                                   @RequestBody AdminUpdateRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(response("User role updated", adminUserService.setRole(id, request.getRole(), authentication.getName())));
    }

    private <T> ApiResponse<T> response(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data)
                .timestamp(Instant.now()).correlationId(UUID.randomUUID()).build();
    }
}
