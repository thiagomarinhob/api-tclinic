package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.LabExamType;
import com.jettech.api.solutions_clinic.model.entity.LabSector;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.LabExamTypeRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateLabExamTypeUseCase implements CreateLabExamTypeUseCase {

    private final LabExamTypeRepository labExamTypeRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public LabExamTypeResponse execute(CreateLabExamTypeRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        LabExamType examType = new LabExamType();
        examType.setTenant(tenant);
        examType.setCode(request.code());
        examType.setName(request.name());
        examType.setSector(request.sector() != null ? request.sector() : LabSector.OTHER);
        examType.setSampleType(request.sampleType());
        examType.setUnit(request.unit());
        examType.setReferenceRangeText(request.referenceRangeText());
        examType.setPreparationInfo(request.preparationInfo());
        examType.setTurnaroundHours(request.turnaroundHours());
        examType.setActive(true);

        examType = labExamTypeRepository.save(examType);
        return toResponse(examType);
    }

    static LabExamTypeResponse toResponse(LabExamType e) {
        return new LabExamTypeResponse(
            e.getId(), e.getTenant().getId(), e.getCode(), e.getName(),
            e.getSector(), e.getSampleType(), e.getUnit(),
            e.getReferenceRangeText(), e.getPreparationInfo(),
            e.getTurnaroundHours(), e.isActive(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
