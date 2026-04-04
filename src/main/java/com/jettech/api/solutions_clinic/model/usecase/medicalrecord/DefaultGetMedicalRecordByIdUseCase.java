package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.model.repository.MedicalRecordRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetMedicalRecordByIdUseCase implements GetMedicalRecordByIdUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final TenantContext tenantContext;
    private final MedicalRecordResponseMapper responseMapper;

    @Override
    public MedicalRecordResponse execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        return medicalRecordRepository.findByIdAndAppointment_TenantId(id, tenantId)
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Prontuário", id));
    }
}
