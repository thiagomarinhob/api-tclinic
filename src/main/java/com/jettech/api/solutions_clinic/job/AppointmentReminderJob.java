package com.jettech.api.solutions_clinic.job;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.service.WhatsAppNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Job que roda a cada 10 minutos e envia lembrete de WhatsApp para pacientes
 * com consulta marcada dentro da antecedência configurada para o tenant
 * (janela de ±10 min para tolerar variações de execução do scheduler).
 *
 * Substitui o envio que era feito no momento do agendamento.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderJob {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /** Metade da janela de tolerância (em minutos). Para cada tenant, o job cobre [alvo-10, alvo+10]. */
    private static final int WINDOW_HALF_MINUTES = 10;

    private final AppointmentRepository appointmentRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final Clock clock;

    @Scheduled(cron = "0 */10 * * * *") // a cada 10 minutos
    @Transactional
    public void sendReminders() {
        LocalDateTime now         = LocalDateTime.now(clock);
        LocalDateTime windowStart = now.plusMinutes(Tenant.MIN_CONFIRMATION_WINDOW_MINUTES - WINDOW_HALF_MINUTES);
        LocalDateTime windowEnd   = now.plusMinutes(Tenant.MAX_CONFIRMATION_WINDOW_MINUTES + WINDOW_HALF_MINUTES);

        log.info("[AppointmentReminderJob] Job disparado - janela de busca: {} ate {}.", windowStart, windowEnd);

        List<Appointment> candidates = appointmentRepository.findAppointmentsForReminder(
                windowStart, windowEnd, AppointmentStatus.AGENDADO);

        if (candidates.isEmpty()) {
            log.info("[AppointmentReminderJob] Nenhum agendamento encontrado na janela.");
            return;
        }

        for (Appointment appointment : candidates) {
            if (!isWithinTenantWindow(appointment, now)) {
                continue;
            }
            try {
                sendWhatsAppReminder(appointment);
                appointment.setReminderSentAt(LocalDateTime.now(clock));
                appointmentRepository.save(appointment);
            } catch (Exception e) {
                log.error("Erro ao enviar lembrete para agendamento {}: {}", appointment.getId(), e.getMessage());
            }
        }
    }

    /** Verifica se o agendamento está dentro da janela [alvo-10, alvo+10] do tenant, onde alvo = now + confirmationWindowMinutes. */
    private boolean isWithinTenantWindow(Appointment appointment, LocalDateTime now) {
        int tenantWindowMinutes = appointment.getTenant().getConfirmationWindowMinutes();
        LocalDateTime target = now.plusMinutes(tenantWindowMinutes);
        LocalDateTime tenantWindowStart = target.minusMinutes(WINDOW_HALF_MINUTES);
        LocalDateTime tenantWindowEnd   = target.plusMinutes(WINDOW_HALF_MINUTES);

        return !appointment.getScheduledAt().isBefore(tenantWindowStart)
                && !appointment.getScheduledAt().isAfter(tenantWindowEnd);
    }

    private void sendWhatsAppReminder(Appointment appointment) {
        Patient patient = appointment.getPatient();

        log.info("[WhatsApp] Processando lembrete — agendamento={} paciente={} consulta={}",
                appointment.getId(), patient.getId(), appointment.getScheduledAt());

        if (patient.getWhatsapp() == null || patient.getWhatsapp().isBlank()) {
            log.info("[WhatsApp] Paciente {} sem WhatsApp cadastrado; lembrete ignorado (agendamento={}).",
                    patient.getId(), appointment.getId());
            return;
        }

        var tenant = appointment.getTenant();
        String nomePaciente    = patient.getFirstName() != null ? patient.getFirstName().trim() : "";
        String nomeClinica     = tenant.getName()  != null ? tenant.getName()  : "";
        String dataConsulta    = appointment.getScheduledAt().format(DATE_FMT);
        String horarioConsulta = appointment.getScheduledAt().format(TIME_FMT);
        String telefoneContato = tenant.getPhone() != null ? tenant.getPhone() : "";

        var messageIdOpt = whatsAppNotificationService.sendAppointmentReminderWithButtonsReturningMessageId(
                patient.getWhatsapp(), nomePaciente, nomeClinica, dataConsulta, horarioConsulta, telefoneContato);

        messageIdOpt.ifPresent(id -> {
            appointment.setWhatsappMessageId(id);
            log.info("[WhatsApp] Lembrete enviado — agendamento={} paciente={} wamid={}",
                    appointment.getId(), patient.getId(), id);
        });

        if (messageIdOpt.isEmpty()) {
            log.warn("[WhatsApp] Lembrete NÃO enviado — agendamento={} paciente={}.",
                    appointment.getId(), patient.getId());
        }
    }
}
