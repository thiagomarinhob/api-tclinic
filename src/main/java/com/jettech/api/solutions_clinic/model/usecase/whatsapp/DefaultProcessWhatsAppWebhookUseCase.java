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
 * Processa o webhook do WhatsApp: ao receber resposta de botão (Confirmar ou Desmarcar)
 * do template confirmar_agendamento, atualiza o status do agendamento correspondente.
 * <p>
 * Estrutura esperada do webhook (resposta de botão):
 * entry[].changes[].value.messages[] com type "button", context.id = id da nossa mensagem,
 * button.text ou button.payload = "Confirmar" ou "Desmarcar".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultProcessWhatsAppWebhookUseCase implements ProcessWhatsAppWebhookUseCase {

    private static final String TYPE_BUTTON = "button";
    private static final String CONFIRMAR = "Confirmar";
    private static final String DESMARCAR = "Desmarcar";

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
            JsonNode entries = root.path("entry");
            if (!entries.isArray()) {
                return;
            }
            for (JsonNode entry : entries) {
                processEntry(entry);
            }
        } catch (Exception e) {
            log.warn("Erro ao processar webhook WhatsApp: {}", e.getMessage());
        }
    }

    private void processEntry(JsonNode entry) {
        JsonNode changes = entry.path("changes");
        if (!changes.isArray()) return;
        for (JsonNode change : changes) {
            JsonNode value = change.path("value");
            JsonNode messages = value.path("messages");
            if (!messages.isArray()) continue;
            for (JsonNode message : messages) {
                processMessage(message);
            }
        }
    }

    private void processMessage(JsonNode message) {
        if (!TYPE_BUTTON.equals(message.path("type").asText(""))) {
            return;
        }
        String referencedMessageId = message.path("context").path("id").asText("").trim();
        if (referencedMessageId.isBlank()) {
            return;
        }
        String buttonText = getButtonText(message);
        if (buttonText.isBlank()) {
            return;
        }
        Optional<Appointment> appointmentOpt = appointmentRepository.findByWhatsappMessageId(referencedMessageId);
        if (appointmentOpt.isEmpty()) {
            appointmentOpt = findAppointmentBySenderPhone(message, referencedMessageId);
            if (appointmentOpt.isEmpty()) {
                log.warn("Webhook WhatsApp: nenhum agendamento encontrado para message_id={} (verifique se whatsapp_message_id foi gravado ao criar o agendamento)", referencedMessageId);
                return;
            }
            Appointment a = appointmentOpt.get();
            a.setWhatsappMessageId(referencedMessageId);
            appointmentRepository.save(a);
            log.info("Webhook WhatsApp: agendamento {} vinculado ao message_id={} (fallback por telefone)", a.getId(), referencedMessageId);
        }
        Appointment appointment = appointmentOpt.get();
        if (CONFIRMAR.equalsIgnoreCase(buttonText)) {
            if (appointment.getStatus() == AppointmentStatus.AGENDADO) {
                appointment.setStatus(AppointmentStatus.CONFIRMADO);
                appointmentRepository.save(appointment);
                notificationCreator.createAppointmentConfirmation(appointment);
                log.info("Agendamento {} confirmado via WhatsApp (message_id={})", appointment.getId(), referencedMessageId);
            }
        } else if (DESMARCAR.equalsIgnoreCase(buttonText)) {
            if (appointment.getStatus() == AppointmentStatus.AGENDADO || appointment.getStatus() == AppointmentStatus.CONFIRMADO) {
                appointment.setStatus(AppointmentStatus.CANCELADO);
                appointment.setCancelledAt(LocalDateTime.now());
                appointmentRepository.save(appointment);
                notificationCreator.createAppointmentCancellation(appointment);
                log.info("Agendamento {} desmarcado via WhatsApp (message_id={})", appointment.getId(), referencedMessageId);
            }
        }
    }

    /**
     * Fallback: quando o agendamento não tem whatsapp_message_id (ex.: criado antes do recurso),
     * busca pelo telefone do remetente o agendamento mais recente AGENDADO ou CONFIRMADO.
     */
    private Optional<Appointment> findAppointmentBySenderPhone(JsonNode message, String referencedMessageId) {
        String from = message.path("from").asText("").trim();
        if (from.isBlank()) {
            return Optional.empty();
        }
        String normalizedPhone = WhatsAppNotificationService.normalizePhone(from);
        if (normalizedPhone == null || normalizedPhone.length() < 10) {
            return Optional.empty();
        }
        List<Patient> patients = patientRepository.findByWhatsappNormalized(normalizedPhone);
        if (patients.isEmpty()) {
            log.debug("Webhook WhatsApp: nenhum paciente com whatsapp normalizado {} (from={})", normalizedPhone, from);
            return Optional.empty();
        }
        List<UUID> patientIds = patients.stream().map(Patient::getId).toList();
        return appointmentRepository.findFirstByPatientIdInAndStatusInOrderByScheduledAtDesc(
                patientIds,
                List.of(AppointmentStatus.AGENDADO, AppointmentStatus.CONFIRMADO)
        );
    }

    private String getButtonText(JsonNode message) {
        JsonNode button = message.path("button");
        if (button.isMissingNode()) return "";
        String text = button.path("text").asText("");
        if (!text.isBlank()) return text;
        return button.path("payload").asText("");
    }
}
