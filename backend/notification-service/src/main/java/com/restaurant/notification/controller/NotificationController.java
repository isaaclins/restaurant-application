package com.restaurant.notification.controller;

import com.restaurant.notification.dto.SendNotificationRequest;
import com.restaurant.notification.entity.EmailTemplate;
import com.restaurant.notification.entity.Notification;
import com.restaurant.notification.entity.NotificationStatus;
import com.restaurant.notification.entity.NotificationType;
import com.restaurant.notification.repository.NotificationRepository;
import com.restaurant.notification.service.EmailService;
import com.restaurant.notification.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Notifications
 * Note: CORS is handled by API Gateway, not here
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    /**
     * Get all notifications (paginated)
     */
    @GetMapping
    public ResponseEntity<Page<Notification>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(notificationRepository.findAll(pageRequest));
    }

    /**
     * Get notifications by recipient email
     */
    @GetMapping("/recipient/{email}")
    public ResponseEntity<Page<Notification>> getByRecipient(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(notificationRepository.findByRecipientEmail(email, pageRequest));
    }

    /**
     * Get notifications by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Notification>> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(notificationRepository.findByOrderId(orderId));
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getById(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Send a notification manually
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendNotification(@RequestBody SendNotificationRequest request) {
        log.info("Manual notification request: {} to {}", request.getType(), request.getRecipientEmail());

        Map<String, Object> variables = request.getTemplateVariables() != null
                ? request.getTemplateVariables()
                : new HashMap<>();

        variables.put("year", LocalDateTime.now().getYear());

        emailService.sendEmail(
                request.getType(),
                request.getRecipientEmail(),
                request.getRecipientName(),
                request.getLanguage(),
                variables,
                request.getOrderId(),
                request.getUserId(),
                request.getReceiptId());

        return ResponseEntity.ok(Map.of("status", "queued", "message", "Notification queued for sending"));
    }

    /**
     * Retry failed notifications
     */
    @PostMapping("/retry-failed")
    public ResponseEntity<Map<String, String>> retryFailed() {
        log.info("Triggering retry of failed notifications");
        emailService.retryFailedNotifications();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Retry triggered"));
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);

        Map<String, Object> stats = new HashMap<>();
        stats.put("sent", notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.SENT, since));
        stats.put("failed", notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.FAILED, since));
        stats.put("pending", notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.PENDING, since));
        stats.put("byType", notificationRepository.countByTypeGrouped(since));

        return ResponseEntity.ok(stats);
    }

    // Email Template endpoints

    /**
     * Get all email templates
     */
    @GetMapping("/templates")
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        return ResponseEntity.ok(emailTemplateService.getAllTemplates());
    }

    /**
     * Get templates by language
     */
    @GetMapping("/templates/language/{language}")
    public ResponseEntity<List<EmailTemplate>> getTemplatesByLanguage(@PathVariable String language) {
        return ResponseEntity.ok(emailTemplateService.getTemplatesByLanguage(language.toUpperCase()));
    }

    /**
     * Get template by type and language
     */
    @GetMapping("/templates/{type}/{language}")
    public ResponseEntity<EmailTemplate> getTemplate(
            @PathVariable String type,
            @PathVariable String language) {
        try {
            NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
            return emailTemplateService.getTemplate(notificationType, language.toUpperCase())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update email template
     */
    @PutMapping("/templates/{id}")
    public ResponseEntity<EmailTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String subject = request.get("subject");
        String htmlTemplate = request.get("htmlTemplate");
        String textTemplate = request.get("textTemplate");

        EmailTemplate updated = emailTemplateService.updateTemplate(id, subject, htmlTemplate, textTemplate);
        return ResponseEntity.ok(updated);
    }

    /**
     * Send a test email using a template
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody Map<String, Object> request) {
        try {
            Long templateId = Long.valueOf(request.get("templateId").toString());
            String recipientEmail = (String) request.get("recipientEmail");
            @SuppressWarnings("unchecked")
            Map<String, String> variables = (Map<String, String>) request.getOrDefault("templateVariables",
                    new HashMap<>());

            EmailTemplate template = emailTemplateService.getTemplateById(templateId);
            if (template == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Template not found"));
            }

            Map<String, Object> templateVars = new HashMap<>(variables);
            templateVars.put("year", LocalDateTime.now().getYear());

            emailService.sendEmail(
                    template.getType(),
                    recipientEmail,
                    "Test User",
                    template.getLanguage(),
                    templateVars,
                    null, null, null);

            return ResponseEntity.ok(Map.of("success", true, "message", "Test email sent"));
        } catch (Exception e) {
            log.error("Failed to send test email", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get SMTP configuration (without password)
     */
    @GetMapping("/smtp-config")
    public ResponseEntity<Map<String, Object>> getSmtpConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", emailService.getSmtpHost());
        config.put("port", emailService.getSmtpPort());
        config.put("username", emailService.getSmtpUsername());
        config.put("fromEmail", emailService.getFromEmail());
        config.put("fromName", emailService.getFromName());
        config.put("useSsl", emailService.isUseSsl());
        config.put("useTls", emailService.isUseTls());
        return ResponseEntity.ok(config);
    }

    /**
     * Update SMTP configuration
     */
    @PutMapping("/smtp-config")
    public ResponseEntity<Map<String, Object>> updateSmtpConfig(@RequestBody Map<String, Object> request) {
        try {
            String host = (String) request.get("host");
            Integer port = request.get("port") != null ? Integer.valueOf(request.get("port").toString()) : null;
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String fromEmail = (String) request.get("fromEmail");
            String fromName = (String) request.get("fromName");
            Boolean useSsl = (Boolean) request.get("useSsl");
            Boolean useTls = (Boolean) request.get("useTls");

            emailService.updateSmtpConfig(host, port, username, password, fromEmail, fromName, useSsl, useTls);

            return ResponseEntity.ok(Map.of("success", true, "message", "SMTP configuration updated"));
        } catch (Exception e) {
            log.error("Failed to update SMTP config", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Test SMTP connection
     */
    @PostMapping("/smtp-config/test")
    public ResponseEntity<Map<String, Object>> testSmtpConnection() {
        try {
            boolean success = emailService.testConnection();
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "SMTP connection successful"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "SMTP connection failed"));
            }
        } catch (Exception e) {
            log.error("SMTP connection test failed", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
