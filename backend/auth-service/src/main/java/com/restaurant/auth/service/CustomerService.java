package com.restaurant.auth.service;

import com.restaurant.auth.dto.*;
import com.restaurant.auth.entity.CustomerAddress;
import com.restaurant.auth.entity.User;
import com.restaurant.auth.repository.CustomerAddressRepository;
import com.restaurant.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Service - Business logic for customer profile management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final UserRepository userRepository;
    private final CustomerAddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get customer profile by user ID
     */
    public CustomerProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CustomerAddress> addresses = addressRepository.findByUserId(userId);

        return mapToProfileResponse(user, addresses);
    }

    /**
     * Update customer profile
     */
    @Transactional
    public CustomerProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);

        List<CustomerAddress> addresses = addressRepository.findByUserId(userId);
        return mapToProfileResponse(saved, addresses);
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    /**
     * Get all addresses for a customer
     */
    public List<AddressResponse> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add a new address
     */
    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        CustomerAddress address = CustomerAddress.builder()
                .user(user)
                .street(request.getStreet())
                .streetNumber(request.getStreetNumber())
                .additionalInfo(request.getAdditionalInfo())
                .postalCode(request.getPostalCode())
                .city(request.getCity())
                .country(request.getCountry() != null ? request.getCountry() : "Schweiz")
                .label(request.getLabel())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .deliveryInstructions(request.getDeliveryInstructions())
                .build();

        CustomerAddress saved = addressRepository.save(address);
        log.info("Address added for user: {}", userId);

        return mapToAddressResponse(saved);
    }

    /**
     * Update an address
     */
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        address.setStreet(request.getStreet());
        address.setStreetNumber(request.getStreetNumber());
        address.setAdditionalInfo(request.getAdditionalInfo());
        address.setPostalCode(request.getPostalCode());
        address.setCity(request.getCity());
        if (request.getCountry() != null)
            address.setCountry(request.getCountry());
        address.setLabel(request.getLabel());
        if (request.getIsDefault() != null)
            address.setIsDefault(request.getIsDefault());
        address.setDeliveryInstructions(request.getDeliveryInstructions());

        CustomerAddress saved = addressRepository.save(address);
        log.info("Address updated for user: {}", userId);

        return mapToAddressResponse(saved);
    }

    /**
     * Delete an address
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        addressRepository.delete(address);
        log.info("Address deleted for user: {}", userId);
    }

    /**
     * Set an address as default
     */
    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Unset current default
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(addr -> {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                });

        address.setIsDefault(true);
        CustomerAddress saved = addressRepository.save(address);

        return mapToAddressResponse(saved);
    }

    // Mapper methods

    private CustomerProfileResponse mapToProfileResponse(User user, List<CustomerAddress> addresses) {
        return CustomerProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .addresses(addresses.stream()
                        .map(this::mapToAddressResponse)
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    private AddressResponse mapToAddressResponse(CustomerAddress address) {
        String fullAddress = String.format("%s %s, %s %s, %s",
                address.getStreet(),
                address.getStreetNumber() != null ? address.getStreetNumber() : "",
                address.getPostalCode(),
                address.getCity(),
                address.getCountry()).replaceAll("\\s+", " ").trim();

        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .streetNumber(address.getStreetNumber())
                .additionalInfo(address.getAdditionalInfo())
                .postalCode(address.getPostalCode())
                .city(address.getCity())
                .country(address.getCountry())
                .label(address.getLabel())
                .isDefault(address.getIsDefault())
                .deliveryInstructions(address.getDeliveryInstructions())
                .fullAddress(fullAddress)
                .build();
    }
}
