package com.restaurant.notification.service;

import com.restaurant.notification.entity.*;
import com.restaurant.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateService templateService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private EmailService emailService;

    private EmailTemplate testTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@restaurant.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Test Restaurant");
        ReflectionTestUtils.setField(emailService, "maxRetryAttempts", 3);

        testTemplate = EmailTemplate.builder()
                .id(1L)
                .type(NotificationType.ORDER_CONFIRMATION)
                .language("DE")
                .subject("Bestellbestätigung - #{orderNumber}")
                .htmlTemplate("<html><body>Hallo ${customerName}!</body></html>")
                .isActive(true)
                .build();
    }

    @Test
    void sendEmail_emailDisabled_shouldLogButNotSend() {
        when(templateService.getTemplate(NotificationType.ORDER_CONFIRMATION, "DE"))
                .thenReturn(Optional.of(testTemplate));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", "Max Mustermann");
        variables.put("orderNumber", "ORD-001");

        emailService.sendEmail(
                NotificationType.ORDER_CONFIRMATION,
                "test@example.com",
                "Max Mustermann",
                "DE",
                variables,
                1L,
                1L,
                null
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getRecipientEmail()).isEqualTo("test@example.com");
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_CONFIRMATION);
        assertThat(saved.getErrorMessage()).contains("Email disabled");
    }

    @Test
    void sendEmail_noTemplate_shouldSaveFailedNotification() {
        when(templateService.getTemplate(NotificationType.ORDER_CONFIRMATION, "DE"))
                .thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        emailService.sendEmail(
                NotificationType.ORDER_CONFIRMATION,
                "test@example.com",
                "Max",
                "DE",
                new HashMap<>(),
                1L,
                1L,
                null
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(saved.getErrorMessage()).contains("No template found");
    }

    @Test
    void sendEmail_shouldSetCorrectChannel() {
        when(templateService.getTemplate(any(), any())).thenReturn(Optional.of(testTemplate));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        emailService.sendEmail(
                NotificationType.RECEIPT_READY,
                "test@example.com",
                "Test User",
                "EN",
                new HashMap<>(),
                null,
                null,
                "REC-001"
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(captor.getValue().getReceiptId()).isEqualTo("REC-001");
    }
}
