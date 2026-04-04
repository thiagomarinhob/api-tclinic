package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.exception.ApiError;
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
public class DefaultUpdateMedicalRecordTemplateUseCase implements UpdateMedicalRecordTemplateUseCase {

    private final MedicalRecordTemplateRepository templateRepository;
    private final ProfessionalRepository professionalRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MedicalRecordTemplateResponse execute(UpdateMedicalRecordTemplateRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        UUID professionalId = tenantContext.getUserIdOrNull() != null
                ? professionalRepository.findByUserIdAndTenantId(tenantContext.getUserIdOrNull(), tenantId)
                        .map(p -> p.getId())
                        .orElse(null)
                : null;

        MedicalRecordTemplate template = templateRepository.findByIdAvailableForTenant(
                        request.id(), tenantId, professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo de prontuário", request.id()));
        if (template.isReadOnly()) {
            throw new ForbiddenException(ApiError.ACCESS_DENIED);
        }

        if (request.name() != null && !request.name().isBlank()) {
            template.setName(request.name());
        }
        if (request.professionalType() != null) {
            template.setProfessionalType(request.professionalType());
        }
        if (request.schema() != null) {
            template.setSchema(objectMapper.valueToTree(request.schema()));
        }
        if (request.active() != null) {
            template.setActive(request.active());
        }

        template = templateRepository.save(template);

        UUID defaultTemplateId = tenantRepository.findById(tenantId)
                .map(Tenant::getDefaultMedicalRecordTemplateId)
                .orElse(null);
        return DefaultCreateMedicalRecordTemplateUseCase.toResponse(template, defaultTemplateId);
    }
}
