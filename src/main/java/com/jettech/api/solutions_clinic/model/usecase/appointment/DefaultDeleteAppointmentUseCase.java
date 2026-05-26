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
import com.jettech.api.solutions_clinic.model.service.AppointmentEmailService;
import com.jettech.api.solutions_clinic.security.TenantContext;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteAppointmentUseCase implements DeleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentEmailService appointmentEmailService;

    @Override
    @Transactional
    public void execute(CancelAppointmentRequest request) throws AuthenticationFailedException {
        Appointment appointment = appointmentRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.id()));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        appointment.setStatus(AppointmentStatus.CANCELADO);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancellationReason(request.reason());
        appointmentRepository.save(appointment);

        appointmentEmailService.sendCancellation(appointment);
    }
}
