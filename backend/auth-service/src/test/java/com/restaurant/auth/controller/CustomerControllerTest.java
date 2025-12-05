package com.restaurant.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.auth.dto.AddressRequest;
import com.restaurant.auth.dto.ChangePasswordRequest;
import com.restaurant.auth.dto.UpdateProfileRequest;
import com.restaurant.auth.entity.User;
import com.restaurant.auth.entity.Role;
import com.restaurant.auth.repository.CustomerAddressRepository;
import com.restaurant.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Customer Controller Integration Tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerAddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .email("customer@test.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Max")
                .lastName("Muster")
                .phone("+41 79 123 45 67")
                .role(Role.CUSTOMER)
                .build());
    }

    // ==================== Profile Tests ====================

    @Test
    void getProfile_Success() throws Exception {
        mockMvc.perform(get("/api/customers/profile")
                .header("X-User-ID", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value("customer@test.com"))
                .andExpect(jsonPath("$.firstName").value("Max"))
                .andExpect(jsonPath("$.lastName").value("Muster"));
    }

    @Test
    void updateProfile_Success() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Hans")
                .lastName("Meier")
                .phone("+41 79 999 99 99")
                .build();

        mockMvc.perform(put("/api/customers/profile")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Hans"))
                .andExpect(jsonPath("$.lastName").value("Meier"))
                .andExpect(jsonPath("$.phone").value("+41 79 999 99 99"));
    }

    @Test
    void updateProfile_ValidationError() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();

        mockMvc.perform(put("/api/customers/profile")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("password123");
        request.setNewPassword("newPassword456");

        mockMvc.perform(post("/api/customers/profile/change-password")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void changePassword_WrongCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword456");

        mockMvc.perform(post("/api/customers/profile/change-password")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ==================== Address Tests ====================

    @Test
    void getAddresses_Empty() throws Exception {
        mockMvc.perform(get("/api/customers/addresses")
                .header("X-User-ID", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addAddress_Success() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .street("Teststrasse")
                .streetNumber("1")
                .postalCode("8000")
                .city("Zürich")
                .country("Schweiz")
                .label("Home")
                .isDefault(true)
                .build();

        mockMvc.perform(post("/api/customers/addresses")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.street").value("Teststrasse"))
                .andExpect(jsonPath("$.streetNumber").value("1"))
                .andExpect(jsonPath("$.postalCode").value("8000"))
                .andExpect(jsonPath("$.city").value("Zürich"))
                .andExpect(jsonPath("$.isDefault").value(true))
                .andExpect(jsonPath("$.fullAddress").isNotEmpty());
    }

    @Test
    void addAddress_ValidationError() throws Exception {
        AddressRequest request = new AddressRequest();

        mockMvc.perform(post("/api/customers/addresses")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAddress_Success() throws Exception {
        // Create address first
        AddressRequest createRequest = AddressRequest.builder()
                .street("Old Street")
                .postalCode("8000")
                .city("Zürich")
                .build();

        String response = mockMvc.perform(post("/api/customers/addresses")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long addressId = objectMapper.readTree(response).get("id").asLong();

        // Update
        AddressRequest updateRequest = AddressRequest.builder()
                .street("New Street")
                .postalCode("8001")
                .city("Zürich")
                .label("Work")
                .build();

        mockMvc.perform(put("/api/customers/addresses/{addressId}", addressId)
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("New Street"))
                .andExpect(jsonPath("$.postalCode").value("8001"))
                .andExpect(jsonPath("$.label").value("Work"));
    }

    @Test
    void deleteAddress_Success() throws Exception {
        // Create address first
        AddressRequest request = AddressRequest.builder()
                .street("To Delete")
                .postalCode("8000")
                .city("Zürich")
                .build();

        String response = mockMvc.perform(post("/api/customers/addresses")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long addressId = objectMapper.readTree(response).get("id").asLong();

        // Delete
        mockMvc.perform(delete("/api/customers/addresses/{addressId}", addressId)
                .header("X-User-ID", testUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void setDefaultAddress_Success() throws Exception {
        // Create two addresses
        AddressRequest request1 = AddressRequest.builder()
                .street("Address 1")
                .postalCode("8000")
                .city("Zürich")
                .isDefault(true)
                .build();

        mockMvc.perform(post("/api/customers/addresses")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        AddressRequest request2 = AddressRequest.builder()
                .street("Address 2")
                .postalCode("8001")
                .city("Zürich")
                .isDefault(false)
                .build();

        String response = mockMvc.perform(post("/api/customers/addresses")
                .header("X-User-ID", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long address2Id = objectMapper.readTree(response).get("id").asLong();

        // Set address 2 as default
        mockMvc.perform(post("/api/customers/addresses/{addressId}/set-default", address2Id)
                .header("X-User-ID", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void getCustomerById_Success() throws Exception {
        mockMvc.perform(get("/api/customers/{customerId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value("customer@test.com"));
    }
}
