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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Processa webhook da Evolution API (V2 / Go).
 *
 * Payload esperado para resposta em texto:
 *   event = "messages.upsert" (case-insensitive)
 *   data.messageType = "conversation" | "extendedTextMessage"
 *   data.message.conversation = "1" (confirmar) | "2" (cancelar)
 *   data.message.extendedTextMessage.contextInfo.stanzaId = ID da mensagem original
 *   data.key.remoteJid = "5511999999999@s.whatsapp.net"
 *   data.key.fromMe = false (ignora mensagens enviadas pelo próprio sistema)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultProcessWhatsAppWebhookUseCase implements ProcessWhatsAppWebhookUseCase {

    private static final String EVENT_MESSAGES_UPSERT = "messages.upsert";
    private static final String TEXT_CONFIRMAR = "1";
    private static final String TEXT_CANCELAR = "2";

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
            if (!EVENT_MESSAGES_UPSERT.equalsIgnoreCase(event)) {
                return;
            }

            JsonNode data = root.path("data");

            // Ignora mensagens enviadas pelo próprio sistema
            if (data.path("key").path("fromMe").asBoolean(false)) {
                return;
            }

            String messageType = data.path("messageType").asText("");
            String remoteJid   = data.path("key").path("remoteJid").asText("").trim();

            String text;
            String stanzaId;
            if ("conversation".equals(messageType)) {
                text     = data.path("message").path("conversation").asText("").trim();
                stanzaId = "";
            } else if ("extendedTextMessage".equals(messageType)) {
                JsonNode ext = data.path("message").path("extendedTextMessage");
                text     = ext.path("text").asText("").trim();
                stanzaId = ext.path("contextInfo").path("stanzaId").asText("").trim();
            } else {
                log.info("[WhatsApp] Webhook ignorado — messageType='{}' não tratado (remoteJid={})", messageType, maskJid(remoteJid));
                return;
            }

            if (!TEXT_CONFIRMAR.equals(text) && !TEXT_CANCELAR.equals(text)) {
                log.debug("[WhatsApp] Webhook ignorado — texto '{}' não é 1 nem 2 (remoteJid={})", text, maskJid(remoteJid));
                return;
            }

            log.info("[WhatsApp] Resposta reconhecida — texto='{}' remoteJid={} stanzaId='{}'",
                    text, maskJid(remoteJid), stanzaId.isBlank() ? "(vazio)" : stanzaId);

            processTextResponse(text, stanzaId, remoteJid);

        } catch (Exception e) {
            log.warn("Erro ao processar webhook Evolution: {}", e.getMessage());
        }
    }

    private void processTextResponse(String text, String stanzaId, String remoteJid) {
        Optional<Appointment> appointmentOpt = Optional.empty();

        if (!stanzaId.isBlank()) {
            appointmentOpt = appointmentRepository.findByWhatsappMessageId(stanzaId);
            if (appointmentOpt.isPresent()) {
                log.info("[WhatsApp] Agendamento {} encontrado por stanzaId={}", appointmentOpt.get().getId(), stanzaId);
            }
        }

        if (appointmentOpt.isEmpty()) {
            log.info("[WhatsApp] Agendamento não encontrado por stanzaId='{}'; tentando fallback por telefone (remoteJid={})",
                    stanzaId.isBlank() ? "(vazio)" : stanzaId, maskJid(remoteJid));
            appointmentOpt = findAppointmentBySenderJid(remoteJid, stanzaId);
            if (appointmentOpt.isEmpty()) {
                log.warn("[WhatsApp] Nenhum agendamento encontrado — stanzaId='{}' remoteJid={}",
                        stanzaId.isBlank() ? "(vazio)" : stanzaId, maskJid(remoteJid));
                return;
            }
            if (!stanzaId.isBlank()) {
                Appointment a = appointmentOpt.get();
                a.setWhatsappMessageId(stanzaId);
                appointmentRepository.save(a);
                log.info("[WhatsApp] Agendamento {} vinculado ao stanzaId={} via fallback por telefone", a.getId(), stanzaId);
            }
        }

        Appointment appointment = appointmentOpt.get();

        if (TEXT_CONFIRMAR.equals(text)) {
            if (appointment.getStatus() == AppointmentStatus.AGENDADO) {
                appointment.setStatus(AppointmentStatus.CONFIRMADO);
                appointmentRepository.save(appointment);
                notificationCreator.createAppointmentConfirmation(appointment);
                log.info("[WhatsApp] Agendamento {} CONFIRMADO via WhatsApp (remoteJid={})", appointment.getId(), maskJid(remoteJid));
            } else {
                log.warn("[WhatsApp] Confirmação ignorada — agendamento={} já está em status={}",
                        appointment.getId(), appointment.getStatus());
            }
        } else if (TEXT_CANCELAR.equals(text)) {
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

    private Optional<Appointment> findAppointmentBySenderJid(String remoteJid, String stanzaId) {
        if (remoteJid.isBlank()) return Optional.empty();
        String phone = remoteJid.replace("@s.whatsapp.net", "");
        String normalizedPhone = WhatsAppNotificationService.normalizePhone(phone);
        if (normalizedPhone == null || normalizedPhone.length() < 10) {
            return Optional.empty();
        }
        List<Patient> patients = patientRepository.findByWhatsappNormalized(normalizedPhone);
        if (patients.isEmpty()) {
            log.warn("[WhatsApp] Nenhum paciente cadastrado com whatsapp={} (remoteJid={})", normalizedPhone, maskJid(remoteJid));
            return Optional.empty();
        }
        log.info("[WhatsApp] {} paciente(s) encontrado(s) para remoteJid={}", patients.size(), maskJid(remoteJid));
        List<UUID> patientIds = patients.stream().map(Patient::getId).toList();
        return appointmentRepository.findFirstByPatientIdInAndStatusInOrderByScheduledAtDesc(
                patientIds,
                List.of(AppointmentStatus.AGENDADO, AppointmentStatus.CONFIRMADO)
        );
    }

    private static String maskJid(String jid) {
        if (jid == null || jid.length() < 4) return "***";
        int atIdx = jid.indexOf('@');
        String number = atIdx > 0 ? jid.substring(0, atIdx) : jid;
        String suffix = atIdx > 0 ? jid.substring(atIdx) : "";
        return "***" + number.substring(Math.max(0, number.length() - 4)) + suffix;
    }
}
