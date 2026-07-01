package com.jettech.api.solutions_clinic.model.usecase.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.model.service.NotificationCreator;
import com.jettech.api.solutions_clinic.model.service.WhatsAppNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processa webhook da Evolution API (Go).
 *
 * Payload esperado para resposta em texto:
 *   event = "Message"
 *   data.Info.Type = "text"
 *   data.Message.conversation = "1"/"sim"/"confirmar"/... (confirmar) | "2"/"cancelar"/"não"/... (cancelar),
 *     opcionalmente combinado com um código de confirmação (ex.: "Confirmar C843")
 *   data.Info.MsgMetaInfo.TargetID = ID da mensagem original (quando é reply)
 *   data.Info.Sender = "5511999999999@s.whatsapp.net"
 *   data.Info.IsFromMe = false
 *
 * Payload esperado para seleção em lista (Evolution Go /send/list):
 *   event = "Message"
 *   data.Info.Type = "listResponse"
 *   data.Message.listResponseMessage.singleSelectReply.selectedRowId = "CONFIRM" | "CANCEL"
 *   data.Info.MsgMetaInfo.TargetID = ID da mensagem original
 *   data.Info.Sender = "5511999999999@s.whatsapp.net"
 *   data.Info.IsFromMe = false
 *
 * Também aceita buttonsResponse para compatibilidade com lembretes enviados por /send/button.
 *
 * Resolução do agendamento (sem depender de "reply"):
 *   1. stanzaId conhecido → age direto (prioridade máxima).
 *   2. Sem stanzaId → busca todos os agendamentos ativos (AGENDADO/CONFIRMADO) cujo paciente
 *      tem o telefone do remetente. 1 candidato → age direto. 2+ candidatos → exige que o texto
 *      contenha um código (ex.: "C843") correspondente a um deles; sem código ou código que não
 *      bate com nenhum candidato, não altera nada (só loga) — nunca "adivinha" o mais recente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultProcessWhatsAppWebhookUseCase implements ProcessWhatsAppWebhookUseCase {

    private static final String EVENT_MESSAGE    = "Message";
    private static final String TEXT_CONFIRMAR   = "1";
    private static final String TEXT_CANCELAR    = "2";
    private static final String BUTTON_CONFIRMAR = "CONFIRM";
    private static final String BUTTON_CANCELAR  = "CANCEL";

    private static final Set<String> CONFIRM_WORDS =
            Set.of("1", "sim", "confirmar", "confirmo", "confirmado", "ok");
    private static final Set<String> CANCEL_WORDS =
            Set.of("2", "cancelar", "nao");
    private static final Pattern CODE_PATTERN = Pattern.compile("(?i)\\bC\\d{3}\\b");
    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.AGENDADO, AppointmentStatus.CONFIRMADO);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;
    private final NotificationCreator notificationCreator;

    @Override
    @Transactional
    public void execute(ProcessWhatsAppWebhookRequest request) {
        if (request.body() == null || request.body().isBlank()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(request.body());

            String event = root.path("event").asText("");
            log.info("[WhatsApp] Evento recebido: '{}'", event);
            if (!EVENT_MESSAGE.equalsIgnoreCase(event)) {
                log.info("[WhatsApp] Evento '{}' ignorado — aguardando apenas Message", event);
                return;
            }

            JsonNode data = root.path("data");
            JsonNode info = data.path("Info");

            if (info.path("IsFromMe").asBoolean(false)) {
                log.info("[WhatsApp] Mensagem ignorada — IsFromMe=true (enviada pelo próprio sistema)");
                return;
            }

            String messageType = info.path("Type").asText("");
            String remoteJid   = info.path("Sender").asText("").trim();
            String stanzaId    = info.path("MsgMetaInfo").path("TargetID").asText("").trim();

            if ("buttonsResponse".equalsIgnoreCase(messageType)) {
                String buttonId = data.path("Message").path("buttonsResponseMessage")
                        .path("selectedButtonId").asText("").trim();
                log.info("[WhatsApp] Clique em botão — buttonId='{}' remoteJid={} stanzaId='{}'",
                        buttonId, maskJid(remoteJid), stanzaId.isBlank() ? "(vazio)" : stanzaId);
                processButtonResponse(buttonId, stanzaId, remoteJid);
                return;
            }

            if ("listResponse".equalsIgnoreCase(messageType)) {
                String rowId = extractSelectedRowId(data.path("Message"));
                log.info("[WhatsApp] Seleção em lista — rowId='{}' remoteJid={} stanzaId='{}'",
                        rowId, maskJid(remoteJid), stanzaId.isBlank() ? "(vazio)" : stanzaId);
                processButtonResponse(rowId, stanzaId, remoteJid);
                return;
            }

            if (!"text".equalsIgnoreCase(messageType)) {
                log.info("[WhatsApp] Webhook ignorado — Type='{}' não tratado (remoteJid={})", messageType, maskJid(remoteJid));
                return;
            }

            String rawText = data.path("Message").path("conversation").asText("").trim();
            Optional<String> intentOpt = parseIntent(rawText);

            if (intentOpt.isEmpty()) {
                log.info("[WhatsApp] Webhook ignorado — texto '{}' sem palavra-chave reconhecida (remoteJid={})",
                        rawText, maskJid(remoteJid));
                return;
            }

            Optional<String> codeOpt = extractConfirmationCode(rawText);

            log.info("[WhatsApp] Resposta reconhecida — intenção='{}' código='{}' remoteJid={} stanzaId='{}'",
                    intentOpt.get(), codeOpt.orElse("(nenhum)"), maskJid(remoteJid), stanzaId.isBlank() ? "(vazio)" : stanzaId);

            resolve(intentOpt.get(), codeOpt, stanzaId, remoteJid);

        } catch (Exception e) {
            log.warn("Erro ao processar webhook Evolution: {}", e.getMessage());
        }
    }

    /** Reconhece a intenção (confirmar/cancelar) em texto livre, ignorando maiúsculas/minúsculas e acentuação. */
    private static Optional<String> parseIntent(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return Optional.empty();
        }
        String normalized = stripAccents(rawText.toLowerCase());
        for (String token : normalized.split("[^\\p{L}\\p{N}]+")) {
            if (CONFIRM_WORDS.contains(token)) {
                return Optional.of(TEXT_CONFIRMAR);
            }
            if (CANCEL_WORDS.contains(token)) {
                return Optional.of(TEXT_CANCELAR);
            }
        }
        return Optional.empty();
    }

    /** Extrai um código de confirmação (ex.: "C843") de qualquer posição do texto, se presente. */
    private static Optional<String> extractConfirmationCode(String rawText) {
        if (rawText == null) {
            return Optional.empty();
        }
        Matcher matcher = CODE_PATTERN.matcher(rawText);
        if (matcher.find()) {
            return Optional.of(matcher.group().toUpperCase());
        }
        return Optional.empty();
    }

    private static String stripAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private void processButtonResponse(String buttonId, String stanzaId, String remoteJid) {
        if (buttonId.isBlank()) {
            log.warn("[WhatsApp] buttonsResponse sem selectedButtonId — remoteJid={}", maskJid(remoteJid));
            return;
        }
        String intent;
        if (BUTTON_CONFIRMAR.equalsIgnoreCase(buttonId)) {
            intent = TEXT_CONFIRMAR;
        } else if (BUTTON_CANCELAR.equalsIgnoreCase(buttonId)) {
            intent = TEXT_CANCELAR;
        } else {
            log.warn("[WhatsApp] buttonId '{}' desconhecido — remoteJid={}", buttonId, maskJid(remoteJid));
            return;
        }
        resolve(intent, Optional.empty(), stanzaId, remoteJid);
    }

    private static String extractSelectedRowId(JsonNode message) {
        String rowId = message.path("listResponseMessage").path("singleSelectReply")
                .path("selectedRowId").asText("").trim();
        if (!rowId.isBlank()) return rowId;

        rowId = message.path("listResponseMessage").path("singleSelectReply")
                .path("selectedRowID").asText("").trim();
        if (!rowId.isBlank()) return rowId;

        rowId = message.path("ListResponseMessage").path("SingleSelectReply")
                .path("SelectedRowID").asText("").trim();
        if (!rowId.isBlank()) return rowId;

        return message.path("listResponseMessage").path("singleSelectReply")
                .path("rowId").asText("").trim();
    }

    private void resolve(String intent, Optional<String> code, String stanzaId, String remoteJid) {
        if (!stanzaId.isBlank()) {
            Optional<Appointment> byStanzaId = appointmentRepository.findByWhatsappMessageId(stanzaId);
            if (byStanzaId.isPresent()) {
                log.info("[WhatsApp] Agendamento {} encontrado por stanzaId={}", byStanzaId.get().getId(), stanzaId);
                applyIntent(byStanzaId.get(), intent, remoteJid);
                return;
            }
            log.info("[WhatsApp] Agendamento não encontrado por stanzaId='{}'; tentando fallback por telefone (remoteJid={})",
                    stanzaId, maskJid(remoteJid));
        }

        List<Appointment> candidates = findCandidatesByPhone(remoteJid);

        if (candidates.isEmpty()) {
            log.warn("[WhatsApp] Nenhum agendamento encontrado — stanzaId='{}' remoteJid={}",
                    stanzaId.isBlank() ? "(vazio)" : stanzaId, maskJid(remoteJid));
            return;
        }

        Appointment appointment;
        if (candidates.size() == 1) {
            appointment = candidates.get(0);
        } else {
            Optional<Appointment> matched = code.flatMap(c -> candidates.stream()
                    .filter(a -> c.equalsIgnoreCase(a.getConfirmationCode()))
                    .findFirst());
            if (matched.isEmpty()) {
                log.warn("[WhatsApp] {} agendamentos ativos encontrados para remoteJid={} e nenhum código válido informado — ação ignorada",
                        candidates.size(), maskJid(remoteJid));
                return;
            }
            appointment = matched.get();
        }

        if (!stanzaId.isBlank()) {
            appointment.setWhatsappMessageId(stanzaId);
            appointmentRepository.save(appointment);
            log.info("[WhatsApp] Agendamento {} vinculado ao stanzaId={} via fallback por telefone", appointment.getId(), stanzaId);
        }

        applyIntent(appointment, intent, remoteJid);
    }

    private void applyIntent(Appointment appointment, String intent, String remoteJid) {
        if (TEXT_CONFIRMAR.equals(intent)) {
            if (appointment.getStatus() == AppointmentStatus.AGENDADO) {
                appointment.setStatus(AppointmentStatus.CONFIRMADO);
                appointmentRepository.save(appointment);
                notificationCreator.createAppointmentConfirmation(appointment);
                log.info("[WhatsApp] Agendamento {} CONFIRMADO via WhatsApp (remoteJid={})", appointment.getId(), maskJid(remoteJid));
            } else {
                log.warn("[WhatsApp] Confirmação ignorada — agendamento={} já está em status={}",
                        appointment.getId(), appointment.getStatus());
            }
        } else if (TEXT_CANCELAR.equals(intent)) {
            if (appointment.getStatus() == AppointmentStatus.AGENDADO || appointment.getStatus() == AppointmentStatus.CONFIRMADO) {
                appointment.setStatus(AppointmentStatus.CANCELADO);
                appointment.setCancelledAt(LocalDateTime.now());
                appointmentRepository.save(appointment);
                notificationCreator.createAppointmentCancellation(appointment);
                log.info("[WhatsApp] Agendamento {} CANCELADO via WhatsApp (remoteJid={})", appointment.getId(), maskJid(remoteJid));
            } else {
                log.warn("[WhatsApp] Cancelamento ignorado — agendamento={} já está em status={}",
                        appointment.getId(), appointment.getStatus());
            }
        }
    }

    private List<Appointment> findCandidatesByPhone(String remoteJid) {
        if (remoteJid.isBlank()) return List.of();
        String phone = remoteJid.replace("@s.whatsapp.net", "");
        String normalizedPhone = WhatsAppNotificationService.normalizePhone(phone);
        if (normalizedPhone == null || normalizedPhone.length() < 10) {
            return List.of();
        }
        String localPhone = normalizedPhone.startsWith("55") ? normalizedPhone.substring(2) : normalizedPhone;
        List<Patient> patients = patientRepository.findByWhatsappNormalized(normalizedPhone, localPhone);
        if (patients.isEmpty()) {
            log.warn("[WhatsApp] Nenhum paciente cadastrado com whatsapp={} (remoteJid={})", normalizedPhone, maskJid(remoteJid));
            return List.of();
        }
        log.info("[WhatsApp] {} paciente(s) encontrado(s) para remoteJid={}", patients.size(), maskJid(remoteJid));
        List<UUID> patientIds = patients.stream().map(Patient::getId).toList();
        return appointmentRepository.findByPatientIdInAndStatusIn(patientIds, ACTIVE_STATUSES);
    }

    private static String maskJid(String jid) {
        if (jid == null || jid.length() < 4) return "***";
        int atIdx = jid.indexOf('@');
        String number = atIdx > 0 ? jid.substring(0, atIdx) : jid;
        String suffix = atIdx > 0 ? jid.substring(atIdx) : "";
        return "***" + number.substring(Math.max(0, number.length() - 4)) + suffix;
    }
}
