package com.restaurant.settings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.settings.dto.UpdateReceiptTemplateRequest;
import com.restaurant.settings.entity.CurrencyPosition;
import com.restaurant.settings.entity.Language;
import com.restaurant.settings.entity.ReceiptTemplate;
import com.restaurant.settings.service.ReceiptTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceiptTemplateController.class)
class ReceiptTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
        germanTemplate.setLabelReceipt("Quittung");
        germanTemplate.setThankYouMessage("Vielen Dank!");
        germanTemplate.setHeaderText("Willkommen!");
        germanTemplate.setFooterText("Danke für Ihren Besuch!");

        englishTemplate = new ReceiptTemplate();
        englishTemplate.setId(2L);
        englishTemplate.setLanguage(Language.EN);
        englishTemplate.setIsDefault(false);
        englishTemplate.setShowLogo(true);
        englishTemplate.setCurrency("CHF");
        englishTemplate.setCurrencyPosition(CurrencyPosition.BEFORE);
        englishTemplate.setLabelTotal("Total");
        englishTemplate.setLabelReceipt("Receipt");
        englishTemplate.setThankYouMessage("Thank you!");
    }

    @Test
    void getLanguages_shouldReturnAllLanguages() throws Exception {
        mockMvc.perform(get("/api/receipt-templates/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].nativeName").exists())
                .andExpect(jsonPath("$[0].englishName").exists());
    }

    @Test
    void getAllTemplates_shouldReturnAllTemplates() throws Exception {
        List<ReceiptTemplate> templates = Arrays.asList(germanTemplate, englishTemplate);
        when(receiptTemplateService.getAllTemplates()).thenReturn(templates);

        mockMvc.perform(get("/api/receipt-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].language", is("DE")))
                .andExpect(jsonPath("$[1].language", is("EN")));
    }

    @Test
    void getTemplate_byId_shouldReturnTemplate() throws Exception {
        when(receiptTemplateService.getTemplateById(1L)).thenReturn(germanTemplate);

        mockMvc.perform(get("/api/receipt-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.language", is("DE")))
                .andExpect(jsonPath("$.isDefault", is(true)))
                .andExpect(jsonPath("$.labelTotal", is("Gesamt")));
    }

    @Test
    void getTemplateByLanguage_shouldReturnTemplate() throws Exception {
        when(receiptTemplateService.getTemplateByLanguage(Language.DE)).thenReturn(germanTemplate);

        mockMvc.perform(get("/api/receipt-templates/language/DE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language", is("DE")))
                .andExpect(jsonPath("$.labelReceipt", is("Quittung")));
    }

    @Test
    void getTemplateByLanguage_invalidLanguage_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/receipt-templates/language/INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDefaultTemplate_shouldReturnDefaultTemplate() throws Exception {
        when(receiptTemplateService.getDefaultTemplate()).thenReturn(germanTemplate);

        mockMvc.perform(get("/api/receipt-templates/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault", is(true)))
                .andExpect(jsonPath("$.language", is("DE")));
    }

    @Test
    void updateTemplate_shouldUpdateAndReturnTemplate() throws Exception {
        UpdateReceiptTemplateRequest request = new UpdateReceiptTemplateRequest();
        request.setThankYouMessage("Neuer Dankestext");
        request.setShowLogo(false);

        germanTemplate.setThankYouMessage("Neuer Dankestext");
        germanTemplate.setShowLogo(false);

        when(receiptTemplateService.updateTemplate(eq(1L), any(UpdateReceiptTemplateRequest.class)))
                .thenReturn(germanTemplate);

        mockMvc.perform(put("/api/receipt-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thankYouMessage", is("Neuer Dankestext")))
                .andExpect(jsonPath("$.showLogo", is(false)));
    }

    @Test
    void resetTemplate_shouldResetAndReturnTemplate() throws Exception {
        when(receiptTemplateService.resetTemplate(1L)).thenReturn(germanTemplate);

        mockMvc.perform(post("/api/receipt-templates/1/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language", is("DE")));
    }

    @Test
    void setAsDefault_shouldSetDefaultAndReturnTemplate() throws Exception {
        germanTemplate.setIsDefault(true);
        when(receiptTemplateService.updateTemplate(eq(1L), any(UpdateReceiptTemplateRequest.class)))
                .thenReturn(germanTemplate);

        mockMvc.perform(post("/api/receipt-templates/1/set-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault", is(true)));
    }
}
