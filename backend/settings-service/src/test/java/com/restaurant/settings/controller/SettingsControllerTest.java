package com.restaurant.settings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.settings.dto.DeliveryAreaRequest;
import com.restaurant.settings.dto.UpdateOpeningHoursRequest;
import com.restaurant.settings.dto.UpdateSettingsRequest;
import com.restaurant.settings.repository.DeliveryAreaRepository;
import com.restaurant.settings.repository.OpeningHoursRepository;
import com.restaurant.settings.repository.RestaurantSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Settings Controller Integration Tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.junit.jupiter.api.TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class SettingsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private RestaurantSettingsRepository settingsRepository;

        @Autowired
        private OpeningHoursRepository openingHoursRepository;

        @Autowired
        private DeliveryAreaRepository deliveryAreaRepository;

        @BeforeEach
        void setUp() {
                deliveryAreaRepository.deleteAll();
                // Settings are initialized by @PostConstruct
        }

        // ==================== Restaurant Settings Tests ====================

        @Test
        @org.junit.jupiter.api.Order(1)
        void getSettings_Success() throws Exception {
                mockMvc.perform(get("/api/settings"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.restaurantName").value("Restaurant App"))
                                .andExpect(jsonPath("$.minimumOrderValue").value(20.00))
                                .andExpect(jsonPath("$.deliveryEnabled").value(true))
                                .andExpect(jsonPath("$.openingHours", hasSize(7)));
        }

        @Test
        @org.junit.jupiter.api.Order(2)
        void updateSettings_Success() throws Exception {
                UpdateSettingsRequest request = UpdateSettingsRequest.builder()
                                .restaurantName("New Restaurant Name")
                                .address("Neue Strasse 1")
                                .minimumOrderValue(new BigDecimal("25.00"))
                                .deliveryFee(new BigDecimal("7.50"))
                                .deliveryEnabled(true)
                                .pickupEnabled(false)
                                .build();

                mockMvc.perform(put("/api/settings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.restaurantName").value("New Restaurant Name"))
                                .andExpect(jsonPath("$.minimumOrderValue").value(25.00))
                                .andExpect(jsonPath("$.pickupEnabled").value(false));
        }

        @Test
        void updateSettings_ValidationError() throws Exception {
                UpdateSettingsRequest request = new UpdateSettingsRequest();
                // Missing required fields

                mockMvc.perform(put("/api/settings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors").isNotEmpty());
        }

        @Test
        void getOpenStatus_Success() throws Exception {
                mockMvc.perform(get("/api/settings/status"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isOpen").isBoolean());
        }

        // ==================== Opening Hours Tests ====================

        @Test
        void getOpeningHours_Success() throws Exception {
                mockMvc.perform(get("/api/settings/hours"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(7)))
                                .andExpect(jsonPath("$[0].dayOfWeek").exists())
                                .andExpect(jsonPath("$[0].dayName").exists());
        }

        @Test
        void updateOpeningHours_Success() throws Exception {
                UpdateOpeningHoursRequest request = UpdateOpeningHoursRequest.builder()
                                .dayOfWeek(DayOfWeek.TUESDAY)
                                .openTime(LocalTime.of(10, 0))
                                .closeTime(LocalTime.of(23, 0))
                                .isClosed(false)
                                .deliveryStartTime(LocalTime.of(10, 30))
                                .deliveryEndTime(LocalTime.of(22, 30))
                                .build();

                mockMvc.perform(put("/api/settings/hours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"))
                                .andExpect(jsonPath("$.openTime").value("10:00:00"))
                                .andExpect(jsonPath("$.closeTime").value("23:00:00"));
        }

        @Test
        void updateOpeningHours_WithBreakTime() throws Exception {
                UpdateOpeningHoursRequest request = UpdateOpeningHoursRequest.builder()
                                .dayOfWeek(DayOfWeek.WEDNESDAY)
                                .openTime(LocalTime.of(11, 0))
                                .closeTime(LocalTime.of(22, 0))
                                .breakStartTime(LocalTime.of(15, 0))
                                .breakEndTime(LocalTime.of(17, 0))
                                .isClosed(false)
                                .build();

                mockMvc.perform(put("/api/settings/hours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.breakStartTime").value("15:00:00"))
                                .andExpect(jsonPath("$.breakEndTime").value("17:00:00"));
        }

        // ==================== Delivery Areas Tests ====================

        @Test
        void addDeliveryArea_Success() throws Exception {
                DeliveryAreaRequest request = DeliveryAreaRequest.builder()
                                .postalCode("8001")
                                .city("Zürich Zentrum")
                                .deliveryFee(new BigDecimal("5.00"))
                                .minimumOrderValue(new BigDecimal("30.00"))
                                .estimatedDeliveryMinutes(30)
                                .isActive(true)
                                .build();

                mockMvc.perform(post("/api/settings/delivery-areas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.postalCode").value("8001"))
                                .andExpect(jsonPath("$.city").value("Zürich Zentrum"))
                                .andExpect(jsonPath("$.deliveryFee").value(5.00));
        }

        @Test
        void addDeliveryArea_Duplicate_ReturnsConflict() throws Exception {
                DeliveryAreaRequest request = DeliveryAreaRequest.builder()
                                .postalCode("8002")
                                .city("Area 1")
                                .deliveryFee(new BigDecimal("5.00"))
                                .build();

                // First request
                mockMvc.perform(post("/api/settings/delivery-areas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                // Duplicate
                mockMvc.perform(post("/api/settings/delivery-areas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message", containsString("already exists")));
        }

        @Test
        void getDeliveryAreas_Success() throws Exception {
                // Add some areas first
                DeliveryAreaRequest request = DeliveryAreaRequest.builder()
                                .postalCode("8003")
                                .city("Test Area")
                                .deliveryFee(new BigDecimal("5.00"))
                                .build();

                mockMvc.perform(post("/api/settings/delivery-areas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/settings/delivery-areas"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        void updateDeliveryArea_Success() throws Exception {
                // Create first
                DeliveryAreaRequest createRequest = DeliveryAreaRequest.builder()
                                .postalCode("8004")
                                .city("Original")
                                .deliveryFee(new BigDecimal("5.00"))
                                .build();

                String response = mockMvc.perform(post("/api/settings/delivery-areas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                Long id = objectMapper.readTree(response).get("id").asLong();

                // Update
                DeliveryAreaRequest updateRequest = DeliveryAreaRequest.builder()
                                .postalCode("8004")
                                .city("Updated Area")
                                .deliveryFee(new BigDecimal("7.00"))
                                .estimatedDeliveryMinutes(45)
                                .build();

                mockMvc.perform(put("/api/settings/delivery-areas/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.city").value("Updated Area"))
                                .andExpect(jsonPath("$.deliveryFee").value(7.00));
        }

        @Test
        void deleteDeliveryArea_Success() throws Exception {
                // Create first
                DeliveryAreaRequest request = DeliveryAreaRequest.builder()
                                .postalCode("8005")
                                .city("To Delete")
                                .deliveryFee(new BigDecimal("5.00"))
                                .build();

                String response = mockMvc.perform(post("/api/settings/delivery-areas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                Long id = objectMapper.readTree(response).get("id").asLong();

                // Delete
                mockMvc.perform(delete("/api/settings/delivery-areas/{id}", id))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteDeliveryArea_NotFound() throws Exception {
                mockMvc.perform(delete("/api/settings/delivery-areas/{id}", 99999))
                                .andExpect(status().isNotFound());
        }

        @Test
        void checkDeliveryAvailability_Available() throws Exception {
                // Add delivery area
                DeliveryAreaRequest request = DeliveryAreaRequest.builder()
                                .postalCode("8006")
                                .city("Available Area")
                                .deliveryFee(new BigDecimal("5.00"))
                                .isActive(true)
                                .build();

                mockMvc.perform(post("/api/settings/delivery-areas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/settings/delivery-areas/check/{postalCode}", "8006"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.postalCode").value("8006"));
        }

        @Test
        void checkDeliveryAvailability_NotAvailable() throws Exception {
                mockMvc.perform(get("/api/settings/delivery-areas/check/{postalCode}", "9999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", containsString("not available")));
        }
}
