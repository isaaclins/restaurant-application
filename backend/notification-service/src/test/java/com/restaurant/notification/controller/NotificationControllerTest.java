package com.restaurant.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.notification.dto.SendNotificationRequest;
import com.restaurant.notification.entity.*;
import com.restaurant.notification.repository.NotificationRepository;
import com.restaurant.notification.service.EmailService;
import com.restaurant.notification.service.EmailTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private EmailTemplateService emailTemplateService;

    private Notification testNotification;
    private EmailTemplate testTemplate;

    @BeforeEach
    void setUp() {
        // Mock EmailService methods that might be called
        when(emailService.getSmtpHost()).thenReturn("smtp.test.com");
        when(emailService.getSmtpPort()).thenReturn(587);
        when(emailService.getSmtpUsername()).thenReturn("test@test.com");
        when(emailService.getFromEmail()).thenReturn("noreply@test.com");
        when(emailService.getFromName()).thenReturn("Test Restaurant");
        when(emailService.isUseTls()).thenReturn(true);
        when(emailService.isUseSsl()).thenReturn(false);

        testNotification = Notification.builder()
                .id(1L)
                .type(NotificationType.ORDER_CONFIRMATION)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .recipientEmail("test@example.com")
                .recipientName("Test User")
                .subject("Test Subject")
                .orderId(100L)
                .createdAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();

        testTemplate = EmailTemplate.builder()
                .id(1L)
                .type(NotificationType.ORDER_CONFIRMATION)
                .language("DE")
                .name("Order Confirmation")
                .subject("Bestellbestätigung")
                .htmlTemplate("<html>Test</html>")
                .isActive(true)
                .build();
    }

    @Test
    void getAllNotifications_shouldReturnPagedNotifications() throws Exception {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(notificationRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(notifications, pageRequest, notifications.size()));

        mockMvc.perform(get("/api/notifications")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].recipientEmail", is("test@example.com")));
    }

    @Test
    void getByRecipient_shouldReturnNotifications() throws Exception {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(notificationRepository.findByRecipientEmail(eq("test@example.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(notifications, pageRequest, notifications.size()));

        mockMvc.perform(get("/api/notifications/recipient/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getByOrderId_shouldReturnNotifications() throws Exception {
        when(notificationRepository.findByOrderId(100L))
                .thenReturn(List.of(testNotification));

        mockMvc.perform(get("/api/notifications/order/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(100)));
    }

    @Test
    void getById_existingNotification_shouldReturnNotification() throws Exception {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.type", is("ORDER_CONFIRMATION")));
    }

    @Test
    void getById_nonExistingNotification_shouldReturn404() throws Exception {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notifications/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sendNotification_shouldQueueNotification() throws Exception {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .type(NotificationType.ORDER_CONFIRMATION)
                .channel(NotificationChannel.EMAIL)
                .recipientEmail("customer@example.com")
                .recipientName("Customer")
                .orderId(123L)
                .language("DE")
                .build();

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("queued")));

        verify(emailService).sendEmail(
                eq(NotificationType.ORDER_CONFIRMATION),
                eq("customer@example.com"),
                eq("Customer"),
                eq("DE"),
                any(),
                eq(123L),
                isNull(),
                isNull());
    }

    @Test
    void retryFailed_shouldTriggerRetry() throws Exception {
        mockMvc.perform(post("/api/notifications/retry-failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        verify(emailService).retryFailedNotifications();
    }

    @Test
    void getStats_shouldReturnStatistics() throws Exception {
        when(notificationRepository.countByStatusAndCreatedAtAfter(eq(NotificationStatus.SENT), any()))
                .thenReturn(10L);
        when(notificationRepository.countByStatusAndCreatedAtAfter(eq(NotificationStatus.FAILED), any()))
                .thenReturn(2L);
        when(notificationRepository.countByStatusAndCreatedAtAfter(eq(NotificationStatus.PENDING), any()))
                .thenReturn(1L);
        when(notificationRepository.countByTypeGrouped(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent", is(10)))
                .andExpect(jsonPath("$.failed", is(2)))
                .andExpect(jsonPath("$.pending", is(1)));
    }

    @Test
    void getAllTemplates_shouldReturnTemplates() throws Exception {
        when(emailTemplateService.getAllTemplates())
                .thenReturn(List.of(testTemplate));

        mockMvc.perform(get("/api/notifications/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].language", is("DE")));
    }

    @Test
    void getTemplatesByLanguage_shouldReturnLanguageSpecificTemplates() throws Exception {
        when(emailTemplateService.getTemplatesByLanguage("DE"))
                .thenReturn(List.of(testTemplate));

        mockMvc.perform(get("/api/notifications/templates/language/DE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateTemplate_shouldUpdateAndReturnTemplate() throws Exception {
        testTemplate.setSubject("Updated Subject");
        when(emailTemplateService.updateTemplate(eq(1L), anyString(), anyString(), anyString()))
                .thenReturn(testTemplate);

        mockMvc.perform(put("/api/notifications/templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "subject", "Updated Subject",
                        "htmlTemplate", "<html>Updated</html>",
                        "textTemplate", "Updated text"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is("Updated Subject")));
    }
}
