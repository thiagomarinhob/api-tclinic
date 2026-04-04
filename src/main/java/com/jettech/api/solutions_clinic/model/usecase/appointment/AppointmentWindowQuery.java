package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consulta de agendamentos em uma janela de tempo.
 * Usado por {@link AvailabilityConflictChecker} para buscar candidatos a conflito (ex.: por profissional ou sala).
 */
@FunctionalInterface
public interface AppointmentWindowQuery {

    List<Appointment> findInWindow(LocalDateTime searchStart, LocalDateTime searchEnd);
}
