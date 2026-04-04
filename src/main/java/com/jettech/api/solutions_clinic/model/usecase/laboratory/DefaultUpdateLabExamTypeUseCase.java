package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.ForbiddenException;
import com.jettech.api.solutions_clinic.model.entity.LabExamType;
import com.jettech.api.solutions_clinic.model.entity.LabSector;
import com.jettech.api.solutions_clinic.model.repository.LabExamTypeRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateLabExamTypeUseCase implements UpdateLabExamTypeUseCase {

    private final LabExamTypeRepository labExamTypeRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public LabExamTypeResponse execute(UpdateLabExamTypeRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        LabExamType examType = labExamTypeRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de exame", request.id()));
        if (!examType.getTenant().getId().equals(tenantId)) {
            throw new ForbiddenException();
        }
        examType.setCode(request.code());
        examType.setName(request.name());
        examType.setSector(request.sector() != null ? request.sector() : LabSector.OTHER);
        examType.setSampleType(request.sampleType());
        examType.setUnit(request.unit());
        examType.setReferenceRangeText(request.referenceRangeText());
        examType.setPreparationInfo(request.preparationInfo());
        examType.setTurnaroundHours(request.turnaroundHours());
        examType.setActive(request.active());
        examType = labExamTypeRepository.save(examType);
        return DefaultCreateLabExamTypeUseCase.toResponse(examType);
    }
}
