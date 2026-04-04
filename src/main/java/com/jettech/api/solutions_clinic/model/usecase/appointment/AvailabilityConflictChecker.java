package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Verifica conflito de disponibilidade entre um novo/alterado agendamento e agendamentos existentes.
 * Usado para profissional e sala com a mesma lógica de janela e sobreposição.
 */
@Component
public class AvailabilityConflictChecker {

    public static final int SEARCH_WINDOW_HOURS_BEFORE = 8;
    public static final int SEARCH_WINDOW_HOURS_AFTER = 1;

    /**
     * Verifica se há conflito de horário com agendamentos retornados pela query.
     *
     * @param scheduledAt         início do agendamento
     * @param durationMinutes     duração em minutos
     * @param excludeAppointmentId id do agendamento a ignorar (ex.: o próprio na edição), ou null
     * @param query               consulta que retorna agendamentos na janela (ex.: por profissional ou sala)
     * @param conflictMessage     mensagem retornada em caso de conflito
     * @return mensagem de conflito ou null se não houver conflito
     */
    public String findConflict(
            LocalDateTime scheduledAt,
            int durationMinutes,
            UUID excludeAppointmentId,
            AppointmentWindowQuery query,
            String conflictMessage
    ) {
        LocalDateTime appointmentEnd = scheduledAt.plusMinutes(durationMinutes);
        LocalDateTime searchStart = scheduledAt.minusHours(SEARCH_WINDOW_HOURS_BEFORE);
        LocalDateTime searchEnd = appointmentEnd.plusHours(SEARCH_WINDOW_HOURS_AFTER);

        List<Appointment> existingAppointments = query.findInWindow(searchStart, searchEnd);

        for (Appointment existing : existingAppointments) {
            if (excludeAppointmentId != null && existing.getId().equals(excludeAppointmentId)) {
                continue;
            }

            LocalDateTime existingStart = existing.getScheduledAt();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());

            if (scheduledAt.isBefore(existingEnd) && existingStart.isBefore(appointmentEnd)) {
                return conflictMessage;
            }
        }

        return null;
    }
}
