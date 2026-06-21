package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCheckAvailabilityUseCase implements CheckAvailabilityUseCase {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalScheduleRepository professionalScheduleRepository;

    @Override
    public Boolean execute(CheckAvailabilityRequest request) {
        log.info("Verificando disponibilidade | professionalId={} | startTime={} | durationMinutes={}",
                request.professionalId(), request.startTime(), request.durationMinutes());

        if (!professionalRepository.existsById(request.professionalId())) {
            throw new EntityNotFoundException("Profissional", request.professionalId());
        }

        LocalDateTime scheduledAt = request.startTime();
        int durationMinutes = request.durationMinutes();
        LocalDateTime appointmentEnd = scheduledAt.plusMinutes(durationMinutes);
        DayOfWeek dayOfWeek = scheduledAt.getDayOfWeek();

        boolean hasSchedule = professionalScheduleRepository
                .findByProfessionalIdAndDayOfWeek(request.professionalId(), dayOfWeek)
                .isPresent();

        if (!hasSchedule) {
            log.info("Profissional sem agenda para o dia | professionalId={} | diaDaSemana={} | disponivel=false",
                    request.professionalId(), dayOfWeek);
            return false;
        }

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
                log.info("Conflito de horário detectado | professionalId={} | horárioSolicitado={} | conflitoCom={}",
                        request.professionalId(), scheduledAt, existing.getId());
                return false;
            }
        }

        log.info("Horário disponível | professionalId={} | startTime={}", request.professionalId(), request.startTime());
        return true;
    }
}
