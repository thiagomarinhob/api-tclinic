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
 * Processa webhook da Evolution API (Go).
 *
 * Payload esperado para resposta em texto:
 *   event = "Message"
 *   data.Info.Type = "text"
 *   data.Message.conversation = "1" (confirmar) | "2" (cancelar)
 *   data.Info.MsgMetaInfo.TargetID = ID da mensagem original (quando é reply)
 *   data.Info.Sender = "5511999999999@s.whatsapp.net"
 *   data.Info.IsFromMe = false
 *
 * Payload esperado para clique em botão (Evolution Go /message/sendButtons/{instance}):
 *   event = "Message"
 *   data.Info.Type = "buttonsResponse"
 *   data.Message.buttonsResponseMessage.selectedButtonId = "CONFIRM" | "CANCEL"
 *   data.Info.MsgMetaInfo.TargetID = ID da mensagem original
 *   data.Info.Sender = "5511999999999@s.whatsapp.net"
 *   data.Info.IsFromMe = false
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

            if (!"text".equalsIgnoreCase(messageType)) {
                log.info("[WhatsApp] Webhook ignorado — Type='{}' não tratado (remoteJid={})", messageType, maskJid(remoteJid));
                return;
            }

            String text = data.path("Message").path("conversation").asText("").trim();

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

    private void processButtonResponse(String buttonId, String stanzaId, String remoteJid) {
        if (buttonId.isBlank()) {
            log.warn("[WhatsApp] buttonsResponse sem selectedButtonId — remoteJid={}", maskJid(remoteJid));
            return;
        }
        String text;
        if (BUTTON_CONFIRMAR.equalsIgnoreCase(buttonId)) {
            text = TEXT_CONFIRMAR;
        } else if (BUTTON_CANCELAR.equalsIgnoreCase(buttonId)) {
            text = TEXT_CANCELAR;
        } else {
            log.warn("[WhatsApp] buttonId '{}' desconhecido — remoteJid={}", buttonId, maskJid(remoteJid));
            return;
        }
        processTextResponse(text, stanzaId, remoteJid);
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
        String localPhone = normalizedPhone.startsWith("55") ? normalizedPhone.substring(2) : normalizedPhone;
        List<Patient> patients = patientRepository.findByWhatsappNormalized(normalizedPhone, localPhone);
        if (patients.isEmpty()) {
            log.warn("[WhatsApp] Nenhum paciente cadastrado com whatsapp={} (remoteJid={})", normalizedPhone, maskJid(remoteJid));
            return Optional.empty();
        }
        log.info("[WhatsApp] {} paciente(s) encontrado(s) para remoteJid={}", patients.size(), maskJid(remoteJid));
        List<UUID> patientIds = patients.stream().map(Patient::getId).toList();
        List<AppointmentStatus> activeStatuses = List.of(AppointmentStatus.AGENDADO, AppointmentStatus.CONFIRMADO);

        Optional<Appointment> byReminder = appointmentRepository
                .findFirstByPatientIdInAndStatusInAndReminderSentAtIsNotNullOrderByReminderSentAtDesc(patientIds, activeStatuses);
        if (byReminder.isPresent()) {
            log.info("[WhatsApp] Fallback: agendamento {} selecionado pelo lembrete mais recente (remoteJid={})",
                    byReminder.get().getId(), maskJid(remoteJid));
            return byReminder;
        }

        log.info("[WhatsApp] Fallback: nenhum agendamento com lembrete enviado; usando scheduledAt DESC (remoteJid={})", maskJid(remoteJid));
        return appointmentRepository.findFirstByPatientIdInAndStatusInOrderByScheduledAtDesc(patientIds, activeStatuses);
    }

    private static String maskJid(String jid) {
        if (jid == null || jid.length() < 4) return "***";
        int atIdx = jid.indexOf('@');
        String number = atIdx > 0 ? jid.substring(0, atIdx) : jid;
        String suffix = atIdx > 0 ? jid.substring(atIdx) : "";
        return "***" + number.substring(Math.max(0, number.length() - 4)) + suffix;
    }
}
