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
import com.jettech.api.solutions_clinic.model.service.AppointmentEmailService;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteAppointmentUseCase implements DeleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentEmailService appointmentEmailService;

    @Override
    @Transactional
    public void execute(CancelAppointmentRequest request) throws AuthenticationFailedException {
        log.info("Cancelando agendamento | appointmentId={}", request.id());

        Appointment appointment = appointmentRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.id()));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }

        log.info("Agendamento encontrado para cancelamento | appointmentId={} | statusAtual={} | patientId={} | professionalId={} | scheduledAt={}",
                appointment.getId(), appointment.getStatus(),
                appointment.getPatient().getId(), appointment.getProfessional().getId(), appointment.getScheduledAt());

        appointment.setStatus(AppointmentStatus.CANCELADO);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancellationReason(request.reason());
        appointmentRepository.save(appointment);

        log.info("Agendamento cancelado | appointmentId={} | motivo={}",
                appointment.getId(), request.reason() != null ? request.reason() : "não informado");

        try {
            appointmentEmailService.sendCancellation(appointment);
            log.info("E-mail de cancelamento enviado | appointmentId={} | patientId={}",
                    appointment.getId(), appointment.getPatient().getId());
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de cancelamento | appointmentId={} | erro={}",
                    appointment.getId(), e.getMessage(), e);
        }
    }
}
