package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordTemplateRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetMedicalRecordTemplateByIdUseCase implements GetMedicalRecordTemplateByIdUseCase {

    private final MedicalRecordTemplateRepository templateRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    public MedicalRecordTemplateResponse execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        UUID professionalId = tenantContext.getUserIdOrNull() != null
                ? professionalRepository.findByUserIdAndTenantId(tenantContext.getUserIdOrNull(), tenantId)
                        .map(p -> p.getId())
                        .orElse(null)
                : null;

        UUID defaultTemplateId = tenantRepository.findById(tenantId)
                .map(Tenant::getDefaultMedicalRecordTemplateId)
                .orElse(null);

        return templateRepository.findByIdAvailableForTenant(id, tenantId, professionalId)
                .map(t -> DefaultCreateMedicalRecordTemplateUseCase.toResponse(t, defaultTemplateId))
                .orElseThrow(() -> new EntityNotFoundException("Modelo de prontuário", id));
    }
}
