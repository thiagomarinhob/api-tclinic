package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.model.entity.MedicalRecord;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultSignMedicalRecordUseCase implements SignMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final TenantContext tenantContext;
    private final MedicalRecordResponseMapper responseMapper;

    @Override
    @Transactional
    public MedicalRecordResponse execute(UUID recordId) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        MedicalRecord record = medicalRecordRepository.findByIdAndAppointment_TenantId(recordId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Prontuário", recordId));

        record.setSignedAt(LocalDateTime.now());
        record = medicalRecordRepository.save(record);

        return responseMapper.toResponse(record);
    }
}
