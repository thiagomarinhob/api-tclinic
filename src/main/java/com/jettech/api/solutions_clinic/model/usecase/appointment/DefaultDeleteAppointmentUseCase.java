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
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultDeleteAppointmentUseCase implements DeleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentEmailService appointmentEmailService;

    @Override
    @Transactional
    public void execute(UUID id) throws AuthenticationFailedException {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", id));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        // Ao invés de deletar, marca como cancelado
        appointment.setStatus(AppointmentStatus.CANCELADO);
        appointment.setCancelledAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        appointmentEmailService.sendCancellation(appointment);
    }
}
