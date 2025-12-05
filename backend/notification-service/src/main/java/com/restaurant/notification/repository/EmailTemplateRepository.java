package com.restaurant.notification.repository;

import com.restaurant.notification.entity.EmailTemplate;
import com.restaurant.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByTypeAndLanguageAndIsActiveTrue(NotificationType type, String language);

    List<EmailTemplate> findByType(NotificationType type);

    List<EmailTemplate> findByLanguage(String language);

    List<EmailTemplate> findByIsActiveTrue();

    boolean existsByTypeAndLanguage(NotificationType type, String language);
}
