package com.restaurant.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.auth.dto.ChangePasswordRequest;
import com.restaurant.auth.dto.LoginRequest;
import com.restaurant.auth.dto.RefreshTokenRequest;
import com.restaurant.auth.dto.RegisterRequest;
import com.restaurant.auth.entity.Role;
import com.restaurant.auth.entity.User;
import com.restaurant.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private User createTestUser(String email, String password) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName("Test")
                .lastName("User")
                .phone("+41791234567")
                .role(Role.CUSTOMER)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("should register new user successfully")
        void shouldRegisterNewUser() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("newuser@example.com");
            request.setPassword("password123");
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setPhone("+41791234567");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken", notNullValue()))
                    .andExpect(jsonPath("$.refreshToken", notNullValue()))
                    .andExpect(jsonPath("$.tokenType", is("Bearer")))
                    .andExpect(jsonPath("$.user.id", notNullValue()))
                    .andExpect(jsonPath("$.user.email", is("newuser@example.com")));

            assertThat(userRepository.findByEmail("newuser@example.com")).isPresent();
        }

        @Test
        @DisplayName("should return 400 when email is invalid")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("invalid-email");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when email is empty")
        void shouldReturn400WhenEmailEmpty() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password too short")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("user@example.com");
            request.setPassword("short");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 409 when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            createTestUser("existing@example.com", "password123");

            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("should login successfully with valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            createTestUser("user@example.com", "password123");

            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken", notNullValue()))
                    .andExpect(jsonPath("$.refreshToken", notNullValue()))
                    .andExpect(jsonPath("$.tokenType", is("Bearer")))
                    .andExpect(jsonPath("$.user.id", notNullValue()))
                    .andExpect(jsonPath("$.user.email", is("user@example.com")));
        }

        @Test
        @DisplayName("should return 401 when password is incorrect")
        void shouldReturn401WhenPasswordIncorrect() throws Exception {
            createTestUser("user@example.com", "password123");

            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongpassword");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 when user not found")
        void shouldReturn401WhenUserNotFound() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("nonexistent@example.com");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when email is empty")
        void shouldReturn400WhenEmailEmpty() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("");
            request.setPassword("password123");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/password")
    class ChangePassword {

        @Test
        @DisplayName("should change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            User user = createTestUser("user@example.com", "oldpassword123");

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("oldpassword123");
            request.setNewPassword("newpassword123");

            mockMvc.perform(put("/api/auth/password")
                    .header("X-User-ID", user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify password changed
            User updatedUser = userRepository.findById(user.getId()).orElseThrow();
            assertThat(passwordEncoder.matches("newpassword123", updatedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("should return 401 when current password is incorrect")
        void shouldReturn401WhenCurrentPasswordIncorrect() throws Exception {
            User user = createTestUser("user@example.com", "correctpassword");

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("wrongpassword");
            request.setNewPassword("newpassword123");

            mockMvc.perform(put("/api/auth/password")
                    .header("X-User-ID", user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when new password too short")
        void shouldReturn400WhenNewPasswordTooShort() throws Exception {
            User user = createTestUser("user@example.com", "oldpassword123");

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("oldpassword123");
            request.setNewPassword("short");

            mockMvc.perform(put("/api/auth/password")
                    .header("X-User-ID", user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 403 when X-User-ID header is missing")
        void shouldReturn403WhenUserIdMissing() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("oldpassword123");
            request.setNewPassword("newpassword123");

            mockMvc.perform(put("/api/auth/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUser {

        @Test
        @DisplayName("should return current user profile")
        void shouldReturnCurrentUserProfile() throws Exception {
            User user = createTestUser("user@example.com", "password123");

            mockMvc.perform(get("/api/auth/me")
                    .header("X-User-ID", user.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                    .andExpect(jsonPath("$.email", is("user@example.com")))
                    .andExpect(jsonPath("$.firstName", is("Test")))
                    .andExpect(jsonPath("$.lastName", is("User")));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            mockMvc.perform(get("/api/auth/me")
                    .header("X-User-ID", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 when X-User-ID header is missing")
        void shouldReturn403WhenUserIdMissing() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshToken {

        @Test
        @DisplayName("should refresh token successfully with valid refresh token")
        void shouldRefreshTokenSuccessfully() throws Exception {
            createTestUser("refresh@example.com", "password123");

            // First login to get refresh token
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("refresh@example.com");
            loginRequest.setPassword("password123");

            String loginResponse = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

            // Use refresh token to get new access token
            RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                    .refreshToken(refreshToken)
                    .build();

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken", notNullValue()))
                    .andExpect(jsonPath("$.refreshToken", notNullValue()))
                    .andExpect(jsonPath("$.user.email", is("refresh@example.com")));
        }

        @Test
        @DisplayName("should return 400 when refresh token is invalid")
        void shouldReturn400WhenRefreshTokenInvalid() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when refresh token is empty")
        void shouldReturn400WhenRefreshTokenEmpty() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest();

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
