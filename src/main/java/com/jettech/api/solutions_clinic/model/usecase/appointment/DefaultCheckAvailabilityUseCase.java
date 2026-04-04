package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCheckAvailabilityUseCase implements CheckAvailabilityUseCase {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalScheduleRepository professionalScheduleRepository;

    @Override
    public Boolean execute(CheckAvailabilityRequest request) {
        if (!professionalRepository.existsById(request.professionalId())) {
            throw new EntityNotFoundException("Profissional", request.professionalId());
        }

        LocalDateTime scheduledAt = request.startTime();
        int durationMinutes = request.durationMinutes();
        LocalDateTime appointmentEnd = scheduledAt.plusMinutes(durationMinutes);
        DayOfWeek dayOfWeek = scheduledAt.getDayOfWeek();

        // Verificar se o profissional tem agenda para este dia da semana
        boolean hasSchedule = professionalScheduleRepository
                .findByProfessionalIdAndDayOfWeek(request.professionalId(), dayOfWeek)
                .isPresent();

        if (!hasSchedule) {
            return false;
        }

        // Verificar conflitos com agendamentos existentes
        LocalDateTime searchStart = scheduledAt.minusHours(8);
        LocalDateTime searchEnd = appointmentEnd.plusHours(1);

        List<Appointment> existingAppointments = appointmentRepository
                .findByProfessionalIdAndScheduledAtBetweenAndStatusNot(
                        request.professionalId(),
                        searchStart,
                        searchEnd,
                        AppointmentStatus.CANCELADO
                );

        for (Appointment existing : existingAppointments) {
            if (request.excludeAppointmentId() != null && existing.getId().equals(request.excludeAppointmentId())) {
                continue;
            }

            LocalDateTime existingStart = existing.getScheduledAt();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());

            if (scheduledAt.isBefore(existingEnd) && existingStart.isBefore(appointmentEnd)) {
                return false;
            }
        }

        return true;
    }
}
