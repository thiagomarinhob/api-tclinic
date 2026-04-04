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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Envio de notificações via WhatsApp Business API (Meta Graph API).
 * Utiliza variável de ambiente WHATSAPP_ACCESS_TOKEN para autenticação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppNotificationService {

    private static final String GRAPH_BASE = "https://graph.facebook.com";
    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private final WhatsAppConfig whatsAppConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Envia uma mensagem de template para um número WhatsApp.
     *
     * @param to Número no formato E.164 (ex: 5511999999999) ou com formatação (ex: (11) 99999-9999)
     * @param templateName Nome do template aprovado no Meta Business (ex: hello_world)
     * @param languageCode Código do idioma (ex: en_US, pt_BR)
     * @return true se enviado com sucesso
     */
    public boolean sendTemplate(String to, String templateName, String languageCode) {
        if (!whatsAppConfig.isConfigured()) {
            log.warn("WhatsApp não configurado; mensagem de template não enviada para {}", maskPhone(to));
            return false;
        }
        if (templateName == null || templateName.isBlank()) {
            log.warn("Nome do template WhatsApp vazio; envio ignorado para {}", maskPhone(to));
            return false;
        }
        if (languageCode == null || languageCode.isBlank()) {
            log.warn("Código de idioma do template WhatsApp vazio; envio ignorado para {}", maskPhone(to));
            return false;
        }

        String normalizedTo = normalizePhone(to);
        if (normalizedTo == null || normalizedTo.length() < 10) {
            log.warn("Número WhatsApp inválido para envio: {}", maskPhone(to));
            return false;
        }

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizedTo,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", languageCode)
                )
        );

        return postMessages(body);
    }

    /**
     * Envia template com variáveis do corpo (body), sem nomes de parâmetro (usa "1", "2", "3"...).
     * Para templates com variáveis nomeadas (ex.: {{nome_paciente}}), use a sobrecarga com parameterNames.
     */
    public boolean sendTemplateBodyVariables(String to, String templateName, String languageCode,
                                            List<String> bodyVariables) {
        if (bodyVariables == null || bodyVariables.isEmpty()) {
            return sendTemplate(to, templateName, languageCode);
        }
        List<String> positionalNames = new java.util.ArrayList<>();
        for (int i = 0; i < bodyVariables.size(); i++) {
            positionalNames.add(String.valueOf(i + 1));
        }
        return sendTemplateBodyVariables(to, templateName, languageCode, positionalNames, bodyVariables);
    }

    /**
     * Envia template com variáveis nomeadas (API v25+).
     * Cada parâmetro inclui type, parameter_name (ex.: nome_paciente) e text.
     * parameterNames e bodyVariables devem ter o mesmo tamanho e ordem do template no Meta.
     */
    public boolean sendTemplateBodyVariables(String to, String templateName, String languageCode,
                                            List<String> parameterNames, List<String> bodyVariables) {
        return sendTemplateBodyVariablesReturningMessageId(to, templateName, languageCode, parameterNames, bodyVariables)
                .isPresent();
    }

    /**
     * Igual a sendTemplateBodyVariables, mas retorna o ID da mensagem (wamid) retornado pela API do WhatsApp.
     * Usado para vincular respostas do webhook (botão Confirmar/Cancelar) ao agendamento.
     */
    public Optional<String> sendTemplateBodyVariablesReturningMessageId(String to, String templateName, String languageCode,
                                                                        List<String> parameterNames, List<String> bodyVariables) {
        if (bodyVariables == null || bodyVariables.isEmpty()) {
            return sendTemplateWithComponentsReturningMessageId(to, templateName, languageCode, null);
        }
        List<Map<String, Object>> parameters = new java.util.ArrayList<>();
        for (int i = 0; i < bodyVariables.size(); i++) {
            String name = (parameterNames != null && i < parameterNames.size() && parameterNames.get(i) != null && !parameterNames.get(i).isBlank())
                    ? parameterNames.get(i)
                    : String.valueOf(i + 1);
            String value = bodyVariables.get(i) != null ? bodyVariables.get(i) : "";
            parameters.add(Map.of(
                    "type", "text",
                    "parameter_name", name,
                    "text", value
            ));
        }
        List<Map<String, Object>> components = List.of(
                Map.of("type", "body", "parameters", parameters)
        );
        return sendTemplateWithComponentsReturningMessageId(to, templateName, languageCode, components);
    }

    /**
     * Envia mensagem de template com parâmetros (ex: botões, variáveis).
     * components: lista de componentes do template conforme documentação da API.
     */
    public boolean sendTemplateWithComponents(String to, String templateName, String languageCode,
                                              List<Map<String, Object>> components) {
        return sendTemplateWithComponentsReturningMessageId(to, templateName, languageCode, components).isPresent();
    }

    /**
     * Envia template com components e retorna o ID da mensagem (wamid) se o envio for bem-sucedido.
     */
    public Optional<String> sendTemplateWithComponentsReturningMessageId(String to, String templateName, String languageCode,
                                                                          List<Map<String, Object>> components) {
        if (!whatsAppConfig.isConfigured()) {
            log.warn("WhatsApp não configurado; mensagem de template não enviada para {}", maskPhone(to));
            return Optional.empty();
        }
        if (templateName == null || templateName.isBlank()) {
            log.warn("Nome do template WhatsApp vazio; envio ignorado para {}", maskPhone(to));
            return Optional.empty();
        }
        if (languageCode == null || languageCode.isBlank()) {
            log.warn("Código de idioma do template WhatsApp vazio; envio ignorado para {}", maskPhone(to));
            return Optional.empty();
        }

        String normalizedTo = normalizePhone(to);
        if (normalizedTo == null || normalizedTo.length() < 10) {
            log.warn("Número WhatsApp inválido para envio: {}", maskPhone(to));
            return Optional.empty();
        }

        Map<String, Object> template = new java.util.HashMap<>(Map.of(
                "name", templateName,
                "language", Map.of("code", languageCode)
        ));
        if (components != null && !components.isEmpty()) {
            template.put("components", components);
        }

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizedTo,
                "type", "template",
                "template", template
        );

        return postMessagesReturningMessageId(body);
    }

    private boolean postMessages(Map<String, Object> body) {
        return postMessagesReturningMessageId(body).isPresent();
    }

    /**
     * Envia a mensagem e retorna o ID (wamid) retornado pela API, ou vazio em caso de falha.
     * Resposta esperada: {"messaging_product":"whatsapp","messages":[{"id":"wamid.xxx"}]}
     */
    private Optional<String> postMessagesReturningMessageId(Map<String, Object> body) {
        String url = GRAPH_BASE + "/" + whatsAppConfig.getApiVersion() + "/"
                + whatsAppConfig.getPhoneNumberId() + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsAppConfig.getAccessToken());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isBlank()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode messages = root.path("messages");
                if (messages.isArray() && messages.size() > 0) {
                    String messageId = messages.get(0).path("id").asText("");
                    if (!messageId.isBlank()) {
                        log.debug("WhatsApp mensagem enviada com sucesso, id={}", messageId);
                        return Optional.of(messageId);
                    }
                }
                log.debug("WhatsApp mensagem enviada com sucesso (resposta sem id)");
                return Optional.empty();
            }
            log.warn("WhatsApp API respondeu com status {}: {}", response.getStatusCode(), response.getBody());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem WhatsApp: {}", e.getMessage());
            throw new ServiceUnavailableException(ApiError.WHATSAPP_SEND_FAILED);
        }
    }

    /**
     * Normaliza número para apenas dígitos (E.164 sem o +).
     * Se o número começar com 0 (ex: 019...), considera que já tem DDI; caso contrário, assume Brasil (55).
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
