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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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
     * Envia mensagem de lembrete de agendamento via Evolution API.
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

        String url = buildSendUrl("text");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", whatsAppConfig.getInstanceToken());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("[WhatsApp] Enviando lembrete → número={} instância={}", maskPhone(normalizedTo), whatsAppConfig.getInstanceName());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.debug("[WhatsApp] Evolution sendText response status={} body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isBlank()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String messageId = extractMessageId(root);
                if (!messageId.isBlank()) {
                    log.info("[WhatsApp] Mensagem aceita pela Evolution — número={} messageId={}", maskPhone(normalizedTo), messageId);
                    return Optional.of(messageId);
                }
                log.warn("[WhatsApp] Evolution retornou 2xx mas sem messageId — número={} response={}", maskPhone(normalizedTo), response.getBody());
                return Optional.empty();
            }
            log.warn("[WhatsApp] Evolution respondeu status={} — número={} body={}", response.getStatusCode(), maskPhone(normalizedTo), response.getBody());
            return Optional.empty();
        } catch (Exception e) {
            log.error("[WhatsApp] Falha ao chamar Evolution — número={} erro={}", maskPhone(normalizedTo), e.getMessage());
            throw new ServiceUnavailableException(ApiError.WHATSAPP_SEND_FAILED);
        }
    }

    /**
     * Envia lembrete de agendamento com botões interativos via Evolution Go.
     * O paciente vê "✅ Confirmar" e "❌ Cancelar" como botões clicáveis.
     * O id do botão clicado (CONFIRM / CANCEL) chega no webhook como buttonsResponseMessage.
     */
    public Optional<String> sendAppointmentReminderWithButtonsReturningMessageId(
            String to,
            String nomePaciente,
            String nomeClinica,
            String dataConsulta,
            String horarioConsulta,
            String telefoneContato) {

        if (!whatsAppConfig.isConfigured()) {
            log.warn("Evolution API não configurada; lembrete (buttons) não enviado para {}", maskPhone(to));
            return Optional.empty();
        }

        String normalizedTo = normalizePhone(to);
        if (normalizedTo == null || normalizedTo.length() < 10) {
            log.warn("Número WhatsApp inválido: {}", maskPhone(to));
            return Optional.empty();
        }

        String descricao = String.format(
                "Olá, %s! Você tem consulta na %s no dia %s, às %s.",
                nomePaciente, nomeClinica, dataConsulta, horarioConsulta);

        Map<String, Object> btnConfirmar = Map.of("type", "reply", "id", "CONFIRM", "displayText", "✅ Confirmar");
        Map<String, Object> btnCancelar  = Map.of("type", "reply", "id", "CANCEL",  "displayText", "❌ Cancelar");

        Map<String, Object> body = Map.of(
                "number",      normalizedTo,
                "title",       "📅 Confirmação de Consulta",
                "description", descricao,
                "footer",      "Dúvidas? " + telefoneContato,
                "buttons",     List.of(btnConfirmar, btnCancelar)
        );

        String url = buildSendUrl("button");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", whatsAppConfig.getInstanceToken());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("[WhatsApp] Enviando lembrete (buttons) → número={} instância={}", maskPhone(normalizedTo), whatsAppConfig.getInstanceName());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.debug("[WhatsApp] Evolution sendButtons response status={} body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isBlank()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String messageId = extractMessageId(root);
                if (!messageId.isBlank()) {
                    log.info("[WhatsApp] Mensagem (buttons) aceita — número={} messageId={}", maskPhone(normalizedTo), messageId);
                    return Optional.of(messageId);
                }
                log.warn("[WhatsApp] Evolution retornou 2xx mas sem messageId (buttons) — número={} response={}", maskPhone(normalizedTo), response.getBody());
                return Optional.empty();
            }
            log.warn("[WhatsApp] Evolution respondeu status={} (buttons) — número={} body={}", response.getStatusCode(), maskPhone(normalizedTo), response.getBody());
            return Optional.empty();
        } catch (Exception e) {
            log.error("[WhatsApp] Falha ao chamar Evolution (buttons) — número={} erro={}", maskPhone(normalizedTo), e.getMessage());
            throw new ServiceUnavailableException(ApiError.WHATSAPP_SEND_FAILED);
        }
    }

    /**
     * Tenta extrair o ID da mensagem da resposta do Evolution API.
     * Cobre a estrutura padrão (key.id) e variações do Evolution Go.
     */
    private static String extractMessageId(JsonNode root) {
        // Formato padrão Evolution V2 / Go: { "key": { "id": "..." } }
        String id = root.path("key").path("id").asText("");
        if (!id.isBlank()) return id;

        // Evolution Go /send/button: { "data": { "Info": { "ID": "..." } } }
        id = root.path("data").path("Info").path("ID").asText("");
        if (!id.isBlank()) return id;

        // Variação sem wrapper data: { "Info": { "ID": "..." } }
        id = root.path("Info").path("ID").asText("");
        if (!id.isBlank()) return id;

        // Fallback: id na raiz — algumas versões do Evolution Go
        id = root.path("id").asText("");
        if (!id.isBlank()) return id;

        // Fallback adicional: { "message": { "key": { "id": "..." } } }
        return root.path("message").path("key").path("id").asText("");
    }

    private String buildSendUrl(String endpoint) {
        String baseUrl = whatsAppConfig.getApiUrl().replaceAll("/+$", "");
        return UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("send", endpoint)
                .toUriString();
    }

    /**
     * Normaliza número para apenas dígitos (E.164 sem o +), corrigindo o 9º dígito
     * de celulares brasileiros armazenados no formato antigo (8 dígitos após o DDD).
     */
    public static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String digits = NON_DIGIT.matcher(phone).replaceAll("");
        if (digits.isEmpty()) return null;

        // Adiciona DDI Brasil se ausente
        if (digits.length() >= 10 && !digits.startsWith("55")) {
            digits = "55" + digits;
        }

        // Celular BR antigo: 55 + DDD(2) + 8 dígitos = 12 → insere 9 após o DDD
        if (digits.length() == 12 && digits.startsWith("55")) {
            digits = digits.substring(0, 4) + "9" + digits.substring(4);
        }

        return digits;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return "***" + phone.substring(Math.max(0, phone.length() - 4));
    }
}
