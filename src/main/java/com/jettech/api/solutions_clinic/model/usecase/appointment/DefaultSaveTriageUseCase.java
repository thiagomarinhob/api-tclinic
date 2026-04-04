package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultSaveTriageUseCase implements SaveTriageUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    @Transactional
    public AppointmentResponse execute(SaveTriageRequest request) throws AuthenticationFailedException {
        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", request.appointmentId()));

        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }

        appointment.setVitalSigns(request.vitalSigns());
        appointment = appointmentRepository.save(appointment);

        return mapper.toResponse(appointment);
    }
}
