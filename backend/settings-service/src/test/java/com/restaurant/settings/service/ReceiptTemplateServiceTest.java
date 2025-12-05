package com.restaurant.settings.service;

import com.restaurant.settings.dto.UpdateReceiptTemplateRequest;
import com.restaurant.settings.entity.CurrencyPosition;
import com.restaurant.settings.entity.Language;
import com.restaurant.settings.entity.ReceiptTemplate;
import com.restaurant.settings.repository.ReceiptTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptTemplateServiceTest {

    @Mock
    private ReceiptTemplateRepository receiptTemplateRepository;

    @InjectMocks
    private ReceiptTemplateService receiptTemplateService;

    private ReceiptTemplate germanTemplate;
    private ReceiptTemplate englishTemplate;

    @BeforeEach
    void setUp() {
        germanTemplate = new ReceiptTemplate();
        germanTemplate.setId(1L);
        germanTemplate.setLanguage(Language.DE);
        germanTemplate.setIsDefault(true);
        germanTemplate.setShowLogo(true);
        germanTemplate.setCurrency("CHF");
        germanTemplate.setCurrencyPosition(CurrencyPosition.BEFORE);
        germanTemplate.setLabelTotal("Gesamt");
        germanTemplate.setThankYouMessage("Vielen Dank!");

        englishTemplate = new ReceiptTemplate();
        englishTemplate.setId(2L);
        englishTemplate.setLanguage(Language.EN);
        englishTemplate.setIsDefault(false);
        englishTemplate.setShowLogo(true);
        englishTemplate.setCurrency("CHF");
        englishTemplate.setCurrencyPosition(CurrencyPosition.BEFORE);
        englishTemplate.setLabelTotal("Total");
        englishTemplate.setThankYouMessage("Thank you!");
    }

    @Test
    void getAllTemplates_shouldReturnAllTemplates() {
        when(receiptTemplateRepository.findAll()).thenReturn(Arrays.asList(germanTemplate, englishTemplate));

        List<ReceiptTemplate> result = receiptTemplateService.getAllTemplates();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReceiptTemplate::getLanguage)
                .containsExactlyInAnyOrder(Language.DE, Language.EN);
    }

    @Test
    void getTemplateByLanguage_existingLanguage_shouldReturnTemplate() {
        when(receiptTemplateRepository.findByLanguage(Language.DE)).thenReturn(Optional.of(germanTemplate));

        ReceiptTemplate result = receiptTemplateService.getTemplateByLanguage(Language.DE);

        assertThat(result).isNotNull();
        assertThat(result.getLanguage()).isEqualTo(Language.DE);
        assertThat(result.getLabelTotal()).isEqualTo("Gesamt");
    }

    @Test
    void getTemplateByLanguage_nonExistingLanguage_shouldCreateDefault() {
        when(receiptTemplateRepository.findByLanguage(Language.FR)).thenReturn(Optional.empty());
        when(receiptTemplateRepository.save(any(ReceiptTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceiptTemplate result = receiptTemplateService.getTemplateByLanguage(Language.FR);

        assertThat(result).isNotNull();
        assertThat(result.getLanguage()).isEqualTo(Language.FR);
        verify(receiptTemplateRepository).save(any(ReceiptTemplate.class));
    }

    @Test
    void getTemplateById_existingId_shouldReturnTemplate() {
        when(receiptTemplateRepository.findById(1L)).thenReturn(Optional.of(germanTemplate));

        ReceiptTemplate result = receiptTemplateService.getTemplateById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getTemplateById_nonExistingId_shouldThrowException() {
        when(receiptTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptTemplateService.getTemplateById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Receipt template not found: 99");
    }

    @Test
    void getDefaultTemplate_shouldReturnDefaultTemplate() {
        when(receiptTemplateRepository.findByIsDefaultTrue()).thenReturn(Optional.of(germanTemplate));

        ReceiptTemplate result = receiptTemplateService.getDefaultTemplate();

        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
        assertThat(result.getLanguage()).isEqualTo(Language.DE);
    }

    @Test
    void updateTemplate_shouldUpdateFields() {
        when(receiptTemplateRepository.findById(1L)).thenReturn(Optional.of(germanTemplate));
        when(receiptTemplateRepository.save(any(ReceiptTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateReceiptTemplateRequest request = new UpdateReceiptTemplateRequest();
        request.setThankYouMessage("Danke für Ihren Besuch!");
        request.setShowLogo(false);
        request.setCurrency("EUR");

        ReceiptTemplate result = receiptTemplateService.updateTemplate(1L, request);

        assertThat(result.getThankYouMessage()).isEqualTo("Danke für Ihren Besuch!");
        assertThat(result.getShowLogo()).isFalse();
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void updateTemplate_setAsDefault_shouldClearOtherDefaults() {
        when(receiptTemplateRepository.findById(2L)).thenReturn(Optional.of(englishTemplate));
        when(receiptTemplateRepository.findByIsDefaultTrue()).thenReturn(Optional.of(germanTemplate));
        when(receiptTemplateRepository.save(any(ReceiptTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateReceiptTemplateRequest request = new UpdateReceiptTemplateRequest();
        request.setIsDefault(true);

        ReceiptTemplate result = receiptTemplateService.updateTemplate(2L, request);

        assertThat(result.getIsDefault()).isTrue();
        verify(receiptTemplateRepository, times(2)).save(any(ReceiptTemplate.class));
    }

    @Test
    void resetTemplate_shouldResetToDefaults() {
        germanTemplate.setThankYouMessage("Custom message");
        germanTemplate.setShowLogo(false);

        when(receiptTemplateRepository.findById(1L)).thenReturn(Optional.of(germanTemplate));
        when(receiptTemplateRepository.save(any(ReceiptTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceiptTemplate result = receiptTemplateService.resetTemplate(1L);

        assertThat(result.getLanguage()).isEqualTo(Language.DE);
        // After reset, should have default German values
        assertThat(result.getThankYouMessage()).isEqualTo("Wir freuen uns auf Ihren nächsten Besuch!");
    }

    @Test
    void languageEnum_shouldHaveCorrectValues() {
        assertThat(Language.DE.getNativeName()).isEqualTo("Deutsch");
        assertThat(Language.DE.getEnglishName()).isEqualTo("German");

        assertThat(Language.EN.getNativeName()).isEqualTo("English");
        assertThat(Language.EN.getEnglishName()).isEqualTo("English");

        assertThat(Language.FR.getNativeName()).isEqualTo("Français");
        assertThat(Language.FR.getEnglishName()).isEqualTo("French");

        assertThat(Language.IT.getNativeName()).isEqualTo("Italiano");
        assertThat(Language.IT.getEnglishName()).isEqualTo("Italian");
    }

    @Test
    void currencyPositionEnum_shouldHaveCorrectValues() {
        assertThat(CurrencyPosition.BEFORE.getExample()).isEqualTo("CHF 45.50");
        assertThat(CurrencyPosition.AFTER.getExample()).isEqualTo("45.50 CHF");
    }
}
