package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultSaveTriageUseCase implements SaveTriageUseCase {

    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final AppointmentResponseMapper mapper;

    @Override
    @Transactional
    public AppointmentResponse execute(SaveTriageRequest request) throws AuthenticationFailedException {
        log.info("Salvando triagem | appointmentId={}", request.appointmentId());

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> {
                    log.warn("Agendamento não encontrado para triagem | appointmentId={}", request.appointmentId());
                    return new EntityNotFoundException("Agendamento", request.appointmentId());
                });

        if (!appointment.getTenant().getId().equals(tenantContext.getRequiredClinicId())) {
            throw new ForbiddenException();
        }

        appointment.setVitalSigns(request.vitalSigns());
        appointment = appointmentRepository.save(appointment);

        log.info("Triagem salva | appointmentId={} | patientId={} | status={}",
                appointment.getId(), appointment.getPatient().getId(), appointment.getStatus());

        return mapper.toResponse(appointment);
    }
}
