package com.restaurant.settings.service;

import com.restaurant.settings.dto.*;
import com.restaurant.settings.entity.DeliveryArea;
import com.restaurant.settings.entity.OpeningHours;
import com.restaurant.settings.entity.RestaurantSettings;
import com.restaurant.settings.entity.SpecialHours;
import com.restaurant.settings.exception.DuplicateEntryException;
import com.restaurant.settings.exception.SettingsNotFoundException;
import com.restaurant.settings.repository.DeliveryAreaRepository;
import com.restaurant.settings.repository.OpeningHoursRepository;
import com.restaurant.settings.repository.RestaurantSettingsRepository;
import com.restaurant.settings.repository.SpecialHoursRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Settings Service - Business logic for restaurant settings management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final RestaurantSettingsRepository settingsRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final DeliveryAreaRepository deliveryAreaRepository;
    private final SpecialHoursRepository specialHoursRepository;

    /**
     * Initialize default settings if not present
     */
    @PostConstruct
    @Transactional
    public void initDefaultSettings() {
        if (settingsRepository.count() == 0) {
            log.info("Initializing default restaurant settings");

            RestaurantSettings defaultSettings = RestaurantSettings.builder()
                    .restaurantName("Restaurant App")
                    .address("Musterstrasse 1")
                    .city("Zürich")
                    .postalCode("8000")
                    .country("Schweiz")
                    .phone("+41 44 123 45 67")
                    .email("info@restaurant-app.ch")
                    .vatNumber("CHE-123.456.789")
                    .vatRate(new BigDecimal("7.7"))
                    .minimumOrderValue(new BigDecimal("20.00"))
                    .deliveryFee(new BigDecimal("5.00"))
                    .freeDeliveryThreshold(new BigDecimal("50.00"))
                    .estimatedDeliveryMinutes(45)
                    .estimatedPickupMinutes(20)
                    .deliveryEnabled(true)
                    .pickupEnabled(true)
                    .dineInEnabled(true)
                    .onlinePaymentEnabled(true)
                    .cashPaymentEnabled(true)
                    .cardPaymentEnabled(true)
                    .isOpen(true)
                    .build();

            settingsRepository.save(defaultSettings);

            // Initialize default opening hours
            initDefaultOpeningHours();
        }
    }

    private void initDefaultOpeningHours() {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (!openingHoursRepository.findByDayOfWeek(day).isPresent()) {
                OpeningHours hours = OpeningHours.builder()
                        .dayOfWeek(day)
                        .openTime(LocalTime.of(11, 0))
                        .closeTime(LocalTime.of(22, 0))
                        .isClosed(day == DayOfWeek.MONDAY) // Closed on Monday
                        .deliveryStartTime(LocalTime.of(11, 30))
                        .deliveryEndTime(LocalTime.of(21, 30))
                        .build();
                openingHoursRepository.save(hours);
            }
        }
    }

    /**
     * Get current restaurant settings
     */
    public RestaurantSettingsResponse getSettings() {
        RestaurantSettings settings = settingsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new SettingsNotFoundException("Restaurant settings not found"));

        List<OpeningHours> hours = openingHoursRepository.findAllByOrderByDayOfWeekAsc();
        List<DeliveryArea> areas = deliveryAreaRepository.findByIsActiveTrue();

        return mapToResponse(settings, hours, areas);
    }

    /**
     * Update restaurant settings
     */
    @Transactional
    public RestaurantSettingsResponse updateSettings(UpdateSettingsRequest request) {
        RestaurantSettings settings = settingsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new SettingsNotFoundException("Restaurant settings not found"));

        // Update fields
        settings.setRestaurantName(request.getRestaurantName());
        settings.setSlogan(request.getSlogan());
        settings.setDescription(request.getDescription());
        settings.setLogoUrl(request.getLogoUrl());
        settings.setAddress(request.getAddress());
        settings.setCity(request.getCity());
        settings.setPostalCode(request.getPostalCode());
        settings.setCountry(request.getCountry());
        settings.setPhone(request.getPhone());
        settings.setEmail(request.getEmail());
        settings.setWebsite(request.getWebsite());
        settings.setVatNumber(request.getVatNumber());
        settings.setVatRate(request.getVatRate());
        settings.setBusinessRegistration(request.getBusinessRegistration());
        settings.setMinimumOrderValue(request.getMinimumOrderValue());
        settings.setDeliveryFee(request.getDeliveryFee());
        settings.setFreeDeliveryThreshold(request.getFreeDeliveryThreshold());
        settings.setEstimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes());
        settings.setEstimatedPickupMinutes(request.getEstimatedPickupMinutes());

        if (request.getDeliveryEnabled() != null)
            settings.setDeliveryEnabled(request.getDeliveryEnabled());
        if (request.getPickupEnabled() != null)
            settings.setPickupEnabled(request.getPickupEnabled());
        if (request.getDineInEnabled() != null)
            settings.setDineInEnabled(request.getDineInEnabled());
        if (request.getOnlinePaymentEnabled() != null)
            settings.setOnlinePaymentEnabled(request.getOnlinePaymentEnabled());
        if (request.getCashPaymentEnabled() != null)
            settings.setCashPaymentEnabled(request.getCashPaymentEnabled());
        if (request.getCardPaymentEnabled() != null)
            settings.setCardPaymentEnabled(request.getCardPaymentEnabled());
        if (request.getIsOpen() != null)
            settings.setIsOpen(request.getIsOpen());

        settings.setClosedMessage(request.getClosedMessage());

        RestaurantSettings saved = settingsRepository.save(settings);
        log.info("Restaurant settings updated");

        return mapToResponse(saved,
                openingHoursRepository.findAllByOrderByDayOfWeekAsc(),
                deliveryAreaRepository.findByIsActiveTrue());
    }

    /**
     * Update opening hours for a day
     */
    @Transactional
    public OpeningHoursResponse updateOpeningHours(UpdateOpeningHoursRequest request) {
        OpeningHours hours = openingHoursRepository.findByDayOfWeek(request.getDayOfWeek())
                .orElse(OpeningHours.builder()
                        .dayOfWeek(request.getDayOfWeek())
                        .build());

        hours.setOpenTime(request.getOpenTime());
        hours.setCloseTime(request.getCloseTime());
        hours.setBreakStartTime(request.getBreakStartTime());
        hours.setBreakEndTime(request.getBreakEndTime());
        if (request.getIsClosed() != null)
            hours.setIsClosed(request.getIsClosed());
        hours.setDeliveryStartTime(request.getDeliveryStartTime());
        hours.setDeliveryEndTime(request.getDeliveryEndTime());

        OpeningHours saved = openingHoursRepository.save(hours);
        log.info("Opening hours updated for {}", request.getDayOfWeek());

        return mapOpeningHoursToResponse(saved);
    }

    /**
     * Get all opening hours
     */
    public List<OpeningHoursResponse> getAllOpeningHours() {
        return openingHoursRepository.findAllByOrderByDayOfWeekAsc().stream()
                .map(this::mapOpeningHoursToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add delivery area
     */
    @Transactional
    public DeliveryAreaResponse addDeliveryArea(DeliveryAreaRequest request) {
        if (deliveryAreaRepository.existsByPostalCode(request.getPostalCode())) {
            throw new DuplicateEntryException(
                    "Delivery area already exists for postal code: " + request.getPostalCode());
        }

        DeliveryArea area = DeliveryArea.builder()
                .postalCode(request.getPostalCode())
                .areaName(request.getAreaName())
                .deliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO)
                .minimumOrderValue(request.getMinimumOrderValue())
                .estimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        DeliveryArea saved = deliveryAreaRepository.save(area);
        log.info("Delivery area added: {}", saved.getPostalCode());

        return mapDeliveryAreaToResponse(saved);
    }

    /**
     * Update delivery area
     */
    @Transactional
    public DeliveryAreaResponse updateDeliveryArea(Long id, DeliveryAreaRequest request) {
        DeliveryArea area = deliveryAreaRepository.findById(id)
                .orElseThrow(() -> new SettingsNotFoundException("Delivery area not found: " + id));

        area.setPostalCode(request.getPostalCode());
        area.setAreaName(request.getAreaName());
        if (request.getDeliveryFee() != null)
            area.setDeliveryFee(request.getDeliveryFee());
        if (request.getMinimumOrderValue() != null)
            area.setMinimumOrderValue(request.getMinimumOrderValue());
        if (request.getEstimatedDeliveryMinutes() != null)
            area.setEstimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes());
        if (request.getIsActive() != null)
            area.setIsActive(request.getIsActive());

        DeliveryArea saved = deliveryAreaRepository.save(area);
        log.info("Delivery area updated: {}", saved.getPostalCode());

        return mapDeliveryAreaToResponse(saved);
    }

    /**
     * Delete delivery area
     */
    @Transactional
    public void deleteDeliveryArea(Long id) {
        if (!deliveryAreaRepository.existsById(id)) {
            throw new SettingsNotFoundException("Delivery area not found: " + id);
        }
        deliveryAreaRepository.deleteById(id);
        log.info("Delivery area deleted: {}", id);
    }

    /**
     * Get all delivery areas
     */
    public List<DeliveryAreaResponse> getAllDeliveryAreas() {
        return deliveryAreaRepository.findAll().stream()
                .map(this::mapDeliveryAreaToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check if delivery is available for postal code
     */
    public DeliveryAreaResponse checkDeliveryAvailability(String postalCode) {
        DeliveryArea area = deliveryAreaRepository.findByPostalCode(postalCode)
                .filter(DeliveryArea::getIsActive)
                .orElseThrow(
                        () -> new SettingsNotFoundException("Delivery not available for postal code: " + postalCode));

        return mapDeliveryAreaToResponse(area);
    }

    /**
     * Check if restaurant is currently open
     */
    public boolean isCurrentlyOpen() {
        RestaurantSettings settings = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
        if (settings == null || !settings.getIsOpen()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Check special hours first
        SpecialHours special = specialHoursRepository.findByDate(today).orElse(null);
        if (special != null) {
            if (special.getIsClosed())
                return false;
            if (special.getOpenTime() != null && special.getCloseTime() != null) {
                return now.isAfter(special.getOpenTime()) && now.isBefore(special.getCloseTime());
            }
        }

        // Check regular hours
        OpeningHours hours = openingHoursRepository.findByDayOfWeek(today.getDayOfWeek()).orElse(null);
        if (hours == null || hours.getIsClosed()) {
            return false;
        }

        if (hours.getOpenTime() == null || hours.getCloseTime() == null) {
            return false;
        }

        // Check if in break time
        if (hours.getBreakStartTime() != null && hours.getBreakEndTime() != null) {
            if (now.isAfter(hours.getBreakStartTime()) && now.isBefore(hours.getBreakEndTime())) {
                return false;
            }
        }

        return now.isAfter(hours.getOpenTime()) && now.isBefore(hours.getCloseTime());
    }

    // Mapper methods

    private RestaurantSettingsResponse mapToResponse(RestaurantSettings settings,
            List<OpeningHours> hours, List<DeliveryArea> areas) {

        return RestaurantSettingsResponse.builder()
                .id(settings.getId())
                .restaurantName(settings.getRestaurantName())
                .slogan(settings.getSlogan())
                .description(settings.getDescription())
                .logoUrl(settings.getLogoUrl())
                .address(settings.getAddress())
                .city(settings.getCity())
                .postalCode(settings.getPostalCode())
                .country(settings.getCountry())
                .phone(settings.getPhone())
                .email(settings.getEmail())
                .website(settings.getWebsite())
                .vatNumber(settings.getVatNumber())
                .vatRate(settings.getVatRate())
                .businessRegistration(settings.getBusinessRegistration())
                .minimumOrderValue(settings.getMinimumOrderValue())
                .deliveryFee(settings.getDeliveryFee())
                .freeDeliveryThreshold(settings.getFreeDeliveryThreshold())
                .estimatedDeliveryMinutes(settings.getEstimatedDeliveryMinutes())
                .estimatedPickupMinutes(settings.getEstimatedPickupMinutes())
                .deliveryEnabled(settings.getDeliveryEnabled())
                .pickupEnabled(settings.getPickupEnabled())
                .dineInEnabled(settings.getDineInEnabled())
                .onlinePaymentEnabled(settings.getOnlinePaymentEnabled())
                .cashPaymentEnabled(settings.getCashPaymentEnabled())
                .cardPaymentEnabled(settings.getCardPaymentEnabled())
                .isOpen(settings.getIsOpen())
                .closedMessage(settings.getClosedMessage())
                .isCurrentlyOpen(isCurrentlyOpen())
                .openingHours(hours.stream()
                        .map(this::mapOpeningHoursToResponse)
                        .collect(Collectors.toList()))
                .deliveryAreas(areas.stream()
                        .map(this::mapDeliveryAreaToResponse)
                        .collect(Collectors.toList()))
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private OpeningHoursResponse mapOpeningHoursToResponse(OpeningHours hours) {
        return OpeningHoursResponse.builder()
                .id(hours.getId())
                .dayOfWeek(hours.getDayOfWeek())
                .dayName(hours.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.GERMAN))
                .openTime(hours.getOpenTime())
                .closeTime(hours.getCloseTime())
                .breakStartTime(hours.getBreakStartTime())
                .breakEndTime(hours.getBreakEndTime())
                .isClosed(hours.getIsClosed())
                .deliveryStartTime(hours.getDeliveryStartTime())
                .deliveryEndTime(hours.getDeliveryEndTime())
                .build();
    }

    private DeliveryAreaResponse mapDeliveryAreaToResponse(DeliveryArea area) {
        return DeliveryAreaResponse.builder()
                .id(area.getId())
                .postalCode(area.getPostalCode())
                .areaName(area.getAreaName())
                .deliveryFee(area.getDeliveryFee())
                .minimumOrderValue(area.getMinimumOrderValue())
                .estimatedDeliveryMinutes(area.getEstimatedDeliveryMinutes())
                .isActive(area.getIsActive())
                .build();
    }
}
