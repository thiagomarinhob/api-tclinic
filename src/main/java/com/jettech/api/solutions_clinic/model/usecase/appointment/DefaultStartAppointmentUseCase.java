package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultStartAppointmentUseCase implements StartAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    @Transactional
    public AppointmentResponse execute(UUID appointmentId) throws AuthenticationFailedException {
        log.info("Iniciando atendimento | appointmentId={}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", appointmentId));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        if (appointment.getStatus() != AppointmentStatus.AGENDADO && appointment.getStatus() != AppointmentStatus.CONFIRMADO) {
            log.warn("Tentativa de iniciar atendimento com status inválido | appointmentId={} | statusAtual={}",
                    appointmentId, appointment.getStatus());
            throw new InvalidStateException(ApiError.INVALID_STATE_APPOINTMENT_STATUS);
        }

        appointment.setStatus(AppointmentStatus.EM_ATENDIMENTO);
        appointment.setStartedAt(LocalDateTime.now());
        appointment = appointmentRepository.save(appointment);

        log.info("Atendimento iniciado | appointmentId={} | patientId={} | professionalId={} | startedAt={}",
                appointment.getId(), appointment.getPatient().getId(),
                appointment.getProfessional().getId(), appointment.getStartedAt());

        return mapper.toResponse(appointment);
    }
}
