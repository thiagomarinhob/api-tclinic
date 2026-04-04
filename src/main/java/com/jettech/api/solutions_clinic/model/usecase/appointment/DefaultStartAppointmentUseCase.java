package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultStartAppointmentUseCase implements StartAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    @Transactional
    public AppointmentResponse execute(UUID appointmentId) throws AuthenticationFailedException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", appointmentId));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        if (appointment.getStatus() != AppointmentStatus.AGENDADO && appointment.getStatus() != AppointmentStatus.CONFIRMADO) {
            throw new InvalidStateException(ApiError.INVALID_STATE_APPOINTMENT_STATUS);
        }

        appointment.setStatus(AppointmentStatus.EM_ATENDIMENTO);
        appointment.setStartedAt(LocalDateTime.now());
        appointment = appointmentRepository.save(appointment);

        return mapper.toResponse(appointment);
    }
}
