package com.restaurant.notification.service;

import com.restaurant.notification.entity.EmailTemplate;
import com.restaurant.notification.entity.NotificationType;
import com.restaurant.notification.repository.EmailTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailTemplateService emailTemplateService;

    private EmailTemplate germanTemplate;
    private EmailTemplate englishTemplate;

    @BeforeEach
    void setUp() {
        germanTemplate = EmailTemplate.builder()
                .id(1L)
                .type(NotificationType.ORDER_CONFIRMATION)
                .language("DE")
                .name("Order Confirmation - DE")
                .subject("Bestellbestätigung - Nr.{orderNumber}")
                .htmlTemplate("<html><body>Hallo ${customerName}!</body></html>")
                .isActive(true)
                .build();

        englishTemplate = EmailTemplate.builder()
                .id(2L)
                .type(NotificationType.ORDER_CONFIRMATION)
                .language("EN")
                .name("Order Confirmation - EN")
                .subject("Order Confirmation - #{orderNumber}")
                .htmlTemplate("<html><body>Hello ${customerName}!</body></html>")
                .isActive(true)
                .build();
    }

    @Test
    void getTemplate_existingTemplate_shouldReturnTemplate() {
        when(emailTemplateRepository.findByTypeAndLanguageAndIsActiveTrue(
                NotificationType.ORDER_CONFIRMATION, "DE"))
                .thenReturn(Optional.of(germanTemplate));

        Optional<EmailTemplate> result = emailTemplateService.getTemplate(
                NotificationType.ORDER_CONFIRMATION, "DE");

        assertThat(result).isPresent();
        assertThat(result.get().getLanguage()).isEqualTo("DE");
        assertThat(result.get().getSubject()).contains("Bestellbestätigung");
    }

    @Test
    void getTemplate_nonExistingLanguage_shouldFallbackToGerman() {
        when(emailTemplateRepository.findByTypeAndLanguageAndIsActiveTrue(
                NotificationType.ORDER_CONFIRMATION, "ES"))
                .thenReturn(Optional.empty());
        when(emailTemplateRepository.findByTypeAndLanguageAndIsActiveTrue(
                NotificationType.ORDER_CONFIRMATION, "DE"))
                .thenReturn(Optional.of(germanTemplate));

        Optional<EmailTemplate> result = emailTemplateService.getTemplate(
                NotificationType.ORDER_CONFIRMATION, "ES");

        assertThat(result).isPresent();
        assertThat(result.get().getLanguage()).isEqualTo("DE");
    }

    @Test
    void getAllTemplates_shouldReturnAllTemplates() {
        when(emailTemplateRepository.findAll())
                .thenReturn(Arrays.asList(germanTemplate, englishTemplate));

        List<EmailTemplate> result = emailTemplateService.getAllTemplates();

        assertThat(result).hasSize(2);
    }

    @Test
    void getTemplatesByLanguage_shouldReturnLanguageSpecificTemplates() {
        when(emailTemplateRepository.findByLanguage("DE"))
                .thenReturn(Arrays.asList(germanTemplate));

        List<EmailTemplate> result = emailTemplateService.getTemplatesByLanguage("DE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLanguage()).isEqualTo("DE");
    }

    @Test
    void updateTemplate_shouldUpdateFields() {
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(germanTemplate));
        when(emailTemplateRepository.save(any(EmailTemplate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EmailTemplate result = emailTemplateService.updateTemplate(
                1L,
                "Neuer Betreff",
                "<html>Neuer Inhalt</html>",
                "Neuer Text"
        );

        assertThat(result.getSubject()).isEqualTo("Neuer Betreff");
        assertThat(result.getHtmlTemplate()).isEqualTo("<html>Neuer Inhalt</html>");
        assertThat(result.getTextTemplate()).isEqualTo("Neuer Text");
    }

    @Test
    void renderTemplate_withVariables_shouldRenderCorrectly() {
        // Simple variable replacement fallback test
        EmailTemplate template = EmailTemplate.builder()
                .htmlTemplate("Hello ${customerName}, your order ${orderNumber} is ready!")
                .build();

        when(templateEngine.process(anyString(), any()))
                .thenThrow(new RuntimeException("Template error")); // Force fallback

        Map<String, Object> variables = Map.of(
                "customerName", "John",
                "orderNumber", "ORD-123"
        );

        String result = emailTemplateService.renderTemplate(template, variables);

        assertThat(result).contains("John");
        assertThat(result).contains("ORD-123");
    }

    @Test
    void notificationType_shouldHaveAllRequiredTypes() {
        assertThat(NotificationType.values()).contains(
                NotificationType.ORDER_CONFIRMATION,
                NotificationType.ORDER_READY,
                NotificationType.PAYMENT_RECEIVED,
                NotificationType.RECEIPT_READY,
                NotificationType.ORDER_PREPARING,
                NotificationType.ORDER_DELIVERED,
                NotificationType.ORDER_CANCELLED
        );
    }
}
