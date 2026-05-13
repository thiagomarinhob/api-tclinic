package com.jettech.api.solutions_clinic.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jettech.api.solutions_clinic.config.WhatsAppConfig;
import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppNotificationService {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private final WhatsAppConfig whatsAppConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Envia mensagem de confirmação de agendamento com botões via Baileys (Evolution API V2).
     *
     * @return Optional com o ID da mensagem retornado pela Evolution, ou vazio em falha
     */
    public Optional<String> sendAppointmentReminderReturningMessageId(
            String to,
            String nomePaciente,
            String nomeClinica,
            String dataConsulta,
            String horarioConsulta,
            String telefoneContato) {

        if (!whatsAppConfig.isConfigured()) {
            log.warn("Evolution API não configurada; lembrete não enviado para {}", maskPhone(to));
            return Optional.empty();
        }

        String normalizedTo = normalizePhone(to);
        if (normalizedTo == null || normalizedTo.length() < 10) {
            log.warn("Número WhatsApp inválido: {}", maskPhone(to));
            return Optional.empty();
        }

        String texto = String.format(
                "Olá, %s! Você tem consulta na %s no dia %s, às %s. Dúvidas: %s.%n%n"
                + "Responda *1* para Confirmar ✅%n"
                + "Responda *2* para Cancelar ❌",
                nomePaciente, nomeClinica, dataConsulta, horarioConsulta, telefoneContato);

        Map<String, Object> body = Map.of(
                "number", normalizedTo,
                "text", texto
        );

        String url = whatsAppConfig.getApiUrl() + "/message/sendText/" + whatsAppConfig.getInstanceName();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", whatsAppConfig.getApiKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isBlank()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String messageId = root.path("key").path("id").asText("");
                if (!messageId.isBlank()) {
                    log.debug("Lembrete enviado via Evolution, messageId={}", messageId);
                    return Optional.of(messageId);
                }
                log.debug("Lembrete enviado via Evolution (resposta sem key.id)");
                return Optional.empty();
            }
            log.warn("Evolution API respondeu status {}: {}", response.getStatusCode(), response.getBody());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao enviar lembrete WhatsApp via Evolution: {}", e.getMessage());
            throw new ServiceUnavailableException(ApiError.WHATSAPP_SEND_FAILED);
        }
    }

    /**
     * Normaliza número para apenas dígitos (E.164 sem o +).
     * Se o número tiver menos de 12 dígitos e não começar com 55, assume Brasil (55).
     */
    public static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String digits = NON_DIGIT.matcher(phone).replaceAll("");
        if (digits.isEmpty()) return null;
        if (digits.length() >= 12) return digits;
        if (digits.length() >= 10 && digits.startsWith("55")) return digits;
        if (digits.length() >= 10) return "55" + digits;
        return digits;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return "***" + phone.substring(Math.max(0, phone.length() - 4));
    }
}
