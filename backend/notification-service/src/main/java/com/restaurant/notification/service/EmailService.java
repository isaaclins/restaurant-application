package com.restaurant.notification.service;

import com.restaurant.notification.entity.*;
import com.restaurant.notification.repository.NotificationRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Service for sending emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final NotificationRepository notificationRepository;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Getter
    @Value("${spring.mail.host:smtp.example.com}")
    private String smtpHost;

    @Getter
    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Getter
    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Getter
    @Value("${notification.email.from:noreply@restaurant.com}")
    private String fromEmail;

    @Getter
    @Value("${notification.email.from-name:Restaurant}")
    private String fromName;

    @Getter
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean useTls;

    @Getter
    @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}")
    private boolean useSsl;

    @Value("${notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    /**
     * Send an email notification
     */
    @Async
    @Transactional
    public void sendEmail(NotificationType type, String recipientEmail, String recipientName,
                          String language, Map<String, Object> variables, Long orderId, Long userId, String receiptId) {
        
        // Create notification record
        Notification notification = Notification.builder()
                .type(type)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.PENDING)
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .orderId(orderId)
                .userId(userId)
                .receiptId(receiptId)
                .language(language != null ? language : "DE")
                .retryCount(0)
                .build();

        // Get template
        Optional<EmailTemplate> templateOpt = templateService.getTemplate(type, language != null ? language : "DE");
        
        if (templateOpt.isEmpty()) {
            log.warn("No email template found for type {} and language {}", type, language);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage("No template found");
            notificationRepository.save(notification);
            return;
        }

        EmailTemplate template = templateOpt.get();
        notification.setTemplateId(template.getId());

        // Render subject and content
        String subject = renderSimpleTemplate(template.getSubject(), variables);
        String htmlContent = templateService.renderTemplate(template, variables);

        notification.setSubject(subject);
        notification.setHtmlContent(htmlContent);

        // Check if email is enabled
        if (!emailEnabled) {
            log.info("Email disabled - would send {} to {}", type, recipientEmail);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage("Email disabled - simulated send");
            notificationRepository.save(notification);
            return;
        }

        // Try to send
        try {
            doSendEmail(recipientEmail, subject, htmlContent);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info("Email sent successfully to {} for {}", recipientEmail, type);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", recipientEmail, e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    /**
     * Retry failed notifications
     */
    @Transactional
    public void retryFailedNotifications() {
        var failedNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, maxRetryAttempts);

        for (Notification notification : failedNotifications) {
            log.info("Retrying notification {} (attempt {})", notification.getId(), notification.getRetryCount() + 1);
            
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setStatus(NotificationStatus.RETRY);
            
            try {
                doSendEmail(notification.getRecipientEmail(), notification.getSubject(), notification.getHtmlContent());
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                log.info("Retry successful for notification {}", notification.getId());
            } catch (Exception e) {
                log.error("Retry failed for notification {}: {}", notification.getId(), e.getMessage());
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
            }
            
            notificationRepository.save(notification);
        }
    }

    private void doSendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail, fromName);
        } catch (java.io.UnsupportedEncodingException e) {
            helper.setFrom(fromEmail);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String renderSimpleTemplate(String template, Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", 
                    entry.getValue() != null ? entry.getValue().toString() : "");
            result = result.replace("${" + entry.getKey() + "}", 
                    entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return result;
    }

    /**
     * Update SMTP configuration dynamically
     */
    public void updateSmtpConfig(String host, Integer port, String username, String password,
                                  String newFromEmail, String newFromName, Boolean newUseSsl, Boolean newUseTls) {
        if (mailSender instanceof JavaMailSenderImpl javaMailSender) {
            if (host != null && !host.isEmpty()) {
                javaMailSender.setHost(host);
            }
            if (port != null) {
                javaMailSender.setPort(port);
            }
            if (username != null) {
                javaMailSender.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                javaMailSender.setPassword(password);
            }
            
            Properties props = javaMailSender.getJavaMailProperties();
            if (newUseTls != null) {
                props.put("mail.smtp.starttls.enable", newUseTls.toString());
            }
            if (newUseSsl != null) {
                props.put("mail.smtp.ssl.enable", newUseSsl.toString());
            }
            
            log.info("SMTP configuration updated: host={}, port={}", 
                    javaMailSender.getHost(), javaMailSender.getPort());
        }
    }

    /**
     * Test SMTP connection
     */
    public boolean testConnection() {
        try {
            if (mailSender instanceof JavaMailSenderImpl javaMailSender) {
                javaMailSender.testConnection();
                log.info("SMTP connection test successful");
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("SMTP connection test failed: {}", e.getMessage());
            throw new RuntimeException("SMTP connection failed: " + e.getMessage());
        }
    }
}
