package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.jettech.api.solutions_clinic.model.entity.MedicalRecordTemplate;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.MedicalRecordTemplateRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetMedicalRecordTemplatesByTenantUseCase implements GetMedicalRecordTemplatesByTenantUseCase {

    private final MedicalRecordTemplateRepository templateRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    public List<MedicalRecordTemplateResponse> execute(GetMedicalRecordTemplatesByTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());

        UUID defaultTemplateId = tenantRepository.findById(request.tenantId())
                .map(Tenant::getDefaultMedicalRecordTemplateId)
                .orElse(null);

        // Escopo: globais + da clínica (professional_id null) + do profissional quando professionalId informado
        List<MedicalRecordTemplate> templates;
        if (request.professionalType() != null && !request.professionalType().isBlank()) {
            templates = templateRepository.findAvailableForTenantAndProfessionalType(
                    request.tenantId(), request.professionalId(), request.professionalType());
        } else {
            templates = templateRepository.findAvailableForTenant(request.tenantId(), request.professionalId());
        }

        return templates.stream()
                .map(t -> DefaultCreateMedicalRecordTemplateUseCase.toResponse(t, defaultTemplateId))
                .toList();
    }
}
