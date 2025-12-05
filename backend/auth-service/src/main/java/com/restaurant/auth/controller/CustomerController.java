package com.restaurant.auth.controller;

import com.restaurant.auth.dto.*;
import com.restaurant.auth.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Customer Controller - REST API for customer profile and address management
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer profile and address management")
public class CustomerController {

    private final CustomerService customerService;

    // ==================== Profile ====================

    @GetMapping("/profile")
    @Operation(summary = "Get current customer profile")
    public ResponseEntity<CustomerProfileResponse> getProfile(
            @RequestHeader("X-User-ID") Long userId) {
        return ResponseEntity.ok(customerService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update customer profile")
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(userId, request));
    }

    @PostMapping("/profile/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        customerService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // ==================== Addresses ====================

    @GetMapping("/addresses")
    @Operation(summary = "Get all customer addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @RequestHeader("X-User-ID") Long userId) {
        return ResponseEntity.ok(customerService.getAddresses(userId));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add a new address")
    public ResponseEntity<AddressResponse> addAddress(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.addAddress(userId, request));
    }

    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update an address")
    public ResponseEntity<AddressResponse> updateAddress(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(customerService.updateAddress(userId, addressId, request));
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<Void> deleteAddress(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long addressId) {
        customerService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addresses/{addressId}/set-default")
    @Operation(summary = "Set an address as default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(customerService.setDefaultAddress(userId, addressId));
    }

    // ==================== Admin: Get customer by ID ====================

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID (Admin)")
    public ResponseEntity<CustomerProfileResponse> getCustomerById(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getProfile(customerId));
    }
}
