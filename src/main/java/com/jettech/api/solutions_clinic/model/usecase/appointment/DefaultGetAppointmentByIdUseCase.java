package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetAppointmentByIdUseCase implements GetAppointmentByIdUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    public AppointmentResponse execute(UUID id) throws AuthenticationFailedException {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", id));
        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }
        return mapper.toResponse(appointment);
    }
}
