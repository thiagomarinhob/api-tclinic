package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.MedicalRecordTemplate;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordTemplateRepository;
import com.jettech.api.solutions_clinic.model.repository.ProfessionalRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultSetMedicalRecordTemplateAsDefaultUseCase implements SetMedicalRecordTemplateAsDefaultUseCase {

    private final MedicalRecordTemplateRepository templateRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public MedicalRecordTemplateResponse execute(UUID id) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        UUID professionalId = tenantContext.getUserIdOrNull() != null
                ? professionalRepository.findByUserIdAndTenantId(tenantContext.getUserIdOrNull(), tenantId)
                        .map(p -> p.getId())
                        .orElse(null)
                : null;

        MedicalRecordTemplate template = templateRepository.findByIdAvailableForTenant(id, tenantId, professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo de prontuário", id));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));
        tenant.setDefaultMedicalRecordTemplateId(template.getId());
        tenantRepository.save(tenant);

        return DefaultCreateMedicalRecordTemplateUseCase.toResponse(template, template.getId());
    }
}
