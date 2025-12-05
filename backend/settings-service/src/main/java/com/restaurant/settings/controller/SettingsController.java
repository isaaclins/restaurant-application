package com.restaurant.settings.controller;

import com.restaurant.settings.dto.*;
import com.restaurant.settings.service.SettingsService;
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
 * Settings Controller - REST API for restaurant settings management
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Restaurant configuration management")
public class SettingsController {

    private final SettingsService settingsService;

    // ==================== Restaurant Settings ====================

    @GetMapping
    @Operation(summary = "Get restaurant settings")
    public ResponseEntity<RestaurantSettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    @Operation(summary = "Update restaurant settings")
    public ResponseEntity<RestaurantSettingsResponse> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateSettings(request));
    }

    @GetMapping("/status")
    @Operation(summary = "Check if restaurant is currently open")
    public ResponseEntity<Map<String, Object>> getOpenStatus() {
        boolean isOpen = settingsService.isCurrentlyOpen();
        RestaurantSettingsResponse settings = settingsService.getSettings();

        return ResponseEntity.ok(Map.of(
                "isOpen", isOpen,
                "closedMessage", settings.getClosedMessage() != null ? settings.getClosedMessage() : ""));
    }

    // ==================== Opening Hours ====================

    @GetMapping("/hours")
    @Operation(summary = "Get all opening hours")
    public ResponseEntity<List<OpeningHoursResponse>> getOpeningHours() {
        return ResponseEntity.ok(settingsService.getAllOpeningHours());
    }

    @PutMapping("/hours")
    @Operation(summary = "Update opening hours for a day")
    public ResponseEntity<OpeningHoursResponse> updateOpeningHours(
            @Valid @RequestBody UpdateOpeningHoursRequest request) {
        return ResponseEntity.ok(settingsService.updateOpeningHours(request));
    }

    // ==================== Delivery Areas ====================

    @GetMapping("/delivery-areas")
    @Operation(summary = "Get all delivery areas")
    public ResponseEntity<List<DeliveryAreaResponse>> getDeliveryAreas() {
        return ResponseEntity.ok(settingsService.getAllDeliveryAreas());
    }

    @PostMapping("/delivery-areas")
    @Operation(summary = "Add a new delivery area")
    public ResponseEntity<DeliveryAreaResponse> addDeliveryArea(
            @Valid @RequestBody DeliveryAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(settingsService.addDeliveryArea(request));
    }

    @PutMapping("/delivery-areas/{id}")
    @Operation(summary = "Update a delivery area")
    public ResponseEntity<DeliveryAreaResponse> updateDeliveryArea(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryAreaRequest request) {
        return ResponseEntity.ok(settingsService.updateDeliveryArea(id, request));
    }

    @DeleteMapping("/delivery-areas/{id}")
    @Operation(summary = "Delete a delivery area")
    public ResponseEntity<Void> deleteDeliveryArea(@PathVariable Long id) {
        settingsService.deleteDeliveryArea(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/delivery-areas/check/{postalCode}")
    @Operation(summary = "Check if delivery is available for a postal code")
    public ResponseEntity<DeliveryAreaResponse> checkDeliveryAvailability(@PathVariable String postalCode) {
        return ResponseEntity.ok(settingsService.checkDeliveryAvailability(postalCode));
    }
}
