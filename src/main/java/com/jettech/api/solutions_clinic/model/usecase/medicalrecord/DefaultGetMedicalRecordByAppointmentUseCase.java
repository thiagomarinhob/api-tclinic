package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetMedicalRecordByAppointmentUseCase implements GetMedicalRecordByAppointmentUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantContext tenantContext;
    private final MedicalRecordResponseMapper responseMapper;

    @Override
    public Optional<MedicalRecordResponse> execute(UUID appointmentId) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();

        var appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", appointmentId));
        if (!appointment.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException(com.jettech.api.solutions_clinic.exception.ApiError.ACCESS_DENIED);
        }

        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .map(responseMapper::toResponse);
    }
}
