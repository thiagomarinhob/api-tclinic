package com.jettech.api.solutions_clinic.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jettech.api.solutions_clinic.config.WhatsAppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppNotificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private WhatsAppNotificationService service;

    @BeforeEach
    void setUp() {
        WhatsAppConfig config = new WhatsAppConfig();
        ReflectionTestUtils.setField(config, "apiUrl", "https://evolution.tclinic.com.br/");
        ReflectionTestUtils.setField(config, "instanceName", "tclinic-prd");
        ReflectionTestUtils.setField(config, "instanceToken", "instance-token");

        service = new WhatsAppNotificationService(config, restTemplate, new ObjectMapper());
    }

    @Test
    void shouldSendAppointmentReminderButtonsToEvolutionMessageEndpoint() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"key\":{\"id\":\"message-123\"}}"));

        var result = service.sendAppointmentReminderWithButtonsReturningMessageId(
                "85999998354",
                "Maria",
                "TClinic",
                "26/06/2026",
                "11:00",
                "(85) 3333-4444"
        );

        assertThat(result).contains("message-123");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass((Class) HttpEntity.class);

        verify(restTemplate).postForEntity(urlCaptor.capture(), requestCaptor.capture(), eq(String.class));

        assertThat(urlCaptor.getValue())
                .isEqualTo("https://evolution.tclinic.com.br/send/button");

        HttpEntity<Map<String, Object>> request = requestCaptor.getValue();
        assertThat(request.getBody()).isNotNull();
        assertThat(request.getHeaders().getFirst("apikey")).isEqualTo("instance-token");
        assertThat(request.getBody())
                .containsEntry("number", "5585999998354")
                .containsEntry("title", "📅 Confirmação de Consulta")
                .containsEntry("footer", "Dúvidas? (85) 3333-4444");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> buttons = (List<Map<String, Object>>) request.getBody().get("buttons");

        assertThat(buttons)
                .containsExactly(
                        Map.of("type", "reply", "id", "CONFIRM", "displayText", "✅ Confirmar"),
                        Map.of("type", "reply", "id", "CANCEL", "displayText", "❌ Cancelar")
                );
    }

    @Test
    void shouldSendAppointmentReminderTextToEvolutionMessageEndpoint() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"key\":{\"id\":\"message-456\"}}"));

        var result = service.sendAppointmentReminderReturningMessageId(
                "85999998354",
                "Maria",
                "TClinic",
                "26/06/2026",
                "11:00",
                "(85) 3333-4444"
        );

        assertThat(result).contains("message-456");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass((Class) HttpEntity.class);

        verify(restTemplate).postForEntity(urlCaptor.capture(), requestCaptor.capture(), eq(String.class));

        assertThat(urlCaptor.getValue())
                .isEqualTo("https://evolution.tclinic.com.br/send/text");

        HttpEntity<Map<String, Object>> request = requestCaptor.getValue();
        assertThat(request.getBody()).isNotNull();
        assertThat(request.getHeaders().getFirst("apikey")).isEqualTo("instance-token");
        assertThat(request.getBody())
                .containsEntry("number", "5585999998354");
        assertThat(request.getBody().get("text").toString())
                .contains("Responda *1* para Confirmar")
                .contains("Responda *2* para Cancelar");
    }
}
