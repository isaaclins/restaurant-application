package com.restaurant.settings.controller;

import com.restaurant.settings.dto.ReceiptTemplateResponse;
import com.restaurant.settings.dto.UpdateReceiptTemplateRequest;
import com.restaurant.settings.entity.Language;
import com.restaurant.settings.entity.ReceiptTemplate;
import com.restaurant.settings.service.ReceiptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Receipt Templates
 * Note: CORS is handled by API Gateway
 */
@RestController
@RequestMapping("/api/receipt-templates")
@RequiredArgsConstructor
@Slf4j
public class ReceiptTemplateController {

    private final ReceiptTemplateService receiptTemplateService;

    /**
     * Get all available languages
     */
    @GetMapping("/languages")
    public ResponseEntity<List<Map<String, String>>> getLanguages() {
        List<Map<String, String>> languages = Arrays.stream(Language.values())
                .map(lang -> Map.of(
                        "code", lang.name(),
                        "nativeName", lang.getNativeName(),
                        "englishName", lang.getEnglishName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(languages);
    }

    /**
     * Get all receipt templates
     */
    @GetMapping
    public ResponseEntity<List<ReceiptTemplateResponse>> getAllTemplates() {
        List<ReceiptTemplateResponse> templates = receiptTemplateService.getAllTemplates()
                .stream()
                .map(ReceiptTemplateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(templates);
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReceiptTemplateResponse> getTemplate(@PathVariable Long id) {
        ReceiptTemplate template = receiptTemplateService.getTemplateById(id);
        return ResponseEntity.ok(ReceiptTemplateResponse.fromEntity(template));
    }

    /**
     * Get template by language
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<ReceiptTemplateResponse> getTemplateByLanguage(@PathVariable String language) {
        try {
            Language lang = Language.valueOf(language.toUpperCase());
            ReceiptTemplate template = receiptTemplateService.getTemplateByLanguage(lang);
            return ResponseEntity.ok(ReceiptTemplateResponse.fromEntity(template));
        } catch (IllegalArgumentException e) {
            log.error("Invalid language: {}", language);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get default template
     */
    @GetMapping("/default")
    public ResponseEntity<ReceiptTemplateResponse> getDefaultTemplate() {
        ReceiptTemplate template = receiptTemplateService.getDefaultTemplate();
        return ResponseEntity.ok(ReceiptTemplateResponse.fromEntity(template));
    }

    /**
     * Update template
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReceiptTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @RequestBody UpdateReceiptTemplateRequest request) {
        log.info("Updating receipt template {}", id);
        ReceiptTemplate template = receiptTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ReceiptTemplateResponse.fromEntity(template));
    }

    /**
     * Reset template to defaults
     */
    @PostMapping("/{id}/reset")
    public ResponseEntity<ReceiptTemplateResponse> resetTemplate(@PathVariable Long id) {
        log.info("Resetting receipt template {} to defaults", id);
        ReceiptTemplate template = receiptTemplateService.resetTemplate(id);
        return ResponseEntity.ok(ReceiptTemplateResponse.fromEntity(template));
    }

    /**
     * Set template as default
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<ReceiptTemplateResponse> setAsDefault(@PathVariable Long id) {
        log.info("Setting receipt template {} as default", id);
        UpdateReceiptTemplateRequest request = new UpdateReceiptTemplateRequest();
        request.setIsDefault(true);
        ReceiptTemplate template = receiptTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ReceiptTemplateResponse.fromEntity(template));
    }
}
