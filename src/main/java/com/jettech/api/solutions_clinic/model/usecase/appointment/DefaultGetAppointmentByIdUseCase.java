package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAppointmentByIdUseCase implements GetAppointmentByIdUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    public AppointmentResponse execute(UUID id) throws AuthenticationFailedException {
        log.info("Buscando agendamento por ID | appointmentId={}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Agendamento não encontrado | appointmentId={}", id);
                    return new EntityNotFoundException("Agendamento", id);
                });
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }

        log.info("Agendamento retornado | appointmentId={} | patientId={} | professionalId={} | status={} | scheduledAt={}",
                appointment.getId(), appointment.getPatient().getId(),
                appointment.getProfessional().getId(), appointment.getStatus(), appointment.getScheduledAt());

        return mapper.toResponse(appointment);
    }
}
