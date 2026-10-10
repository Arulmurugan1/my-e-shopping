package com.myeshopping.authservice.controller;

import com.myeshopping.authservice.dto.ApiResponse;
import com.myeshopping.authservice.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/** Service-to-service lookups. The API gateway blocks /api/v1/auth/internal/** so these are not reachable from the browser. */
@RestController
@RequestMapping("/api/v1/auth/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/{id}/email")
    public ResponseEntity<ApiResponse<String>> email(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("User email fetched successfully")
                .data(adminUserService.getEmail(id)).timestamp(Instant.now()).correlationId(UUID.randomUUID()).build());
    }
}
