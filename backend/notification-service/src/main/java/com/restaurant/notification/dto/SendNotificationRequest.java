package com.restaurant.notification.dto;

import com.restaurant.notification.entity.NotificationChannel;
import com.restaurant.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request to send a notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    private NotificationType type;
    private NotificationChannel channel;
    private String recipientEmail;
    private String recipientName;
    private String recipientPhone;
    private String subject;
    private String content;
    private Long orderId;
    private Long userId;
    private String receiptId;
    private String language;
    private Map<String, Object> templateVariables;
}
