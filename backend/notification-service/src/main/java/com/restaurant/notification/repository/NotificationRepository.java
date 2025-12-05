package com.restaurant.notification.repository;

import com.restaurant.notification.entity.Notification;
import com.restaurant.notification.entity.NotificationChannel;
import com.restaurant.notification.entity.NotificationStatus;
import com.restaurant.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientEmail(String email, Pageable pageable);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    List<Notification> findByOrderId(Long orderId);

    List<Notification> findByUserId(Long userId);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.createdAt < :before")
    List<Notification> findStaleNotifications(NotificationStatus status, LocalDateTime before);

    long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime after);

    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.createdAt >= :since GROUP BY n.type")
    List<Object[]> countByTypeGrouped(LocalDateTime since);
}
