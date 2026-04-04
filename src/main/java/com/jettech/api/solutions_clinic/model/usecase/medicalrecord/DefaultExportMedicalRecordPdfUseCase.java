package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.MedicalRecord;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import com.jettech.api.solutions_clinic.service.MedicalRecordPdfService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultExportMedicalRecordPdfUseCase implements ExportMedicalRecordPdfUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final TenantContext tenantContext;
    private final MedicalRecordPdfService medicalRecordPdfService;

    @Override
    @Transactional(readOnly = true)
    public byte[] execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        MedicalRecord record = medicalRecordRepository.findByIdAndAppointment_TenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Prontuário", id));
        return medicalRecordPdfService.generatePdf(record);
    }
}
