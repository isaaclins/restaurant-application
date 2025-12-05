package com.restaurant.settings.repository;

import com.restaurant.settings.entity.Language;
import com.restaurant.settings.entity.ReceiptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptTemplateRepository extends JpaRepository<ReceiptTemplate, Long> {

    Optional<ReceiptTemplate> findByLanguage(Language language);

    boolean existsByLanguage(Language language);

    Optional<ReceiptTemplate> findByIsDefaultTrue();
}
