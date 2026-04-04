package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.repository.LabExamTypeRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetLabExamTypesByTenantUseCase implements GetLabExamTypesByTenantUseCase {

    private final LabExamTypeRepository labExamTypeRepository;
    private final TenantContext tenantContext;

    @Override
    public Page<LabExamTypeResponse> execute(GetLabExamTypesByTenantRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        PageRequest pageable = PageRequest.of(request.page(), request.size());
        return labExamTypeRepository
            .findByTenantWithFilters(tenantId, request.search(), request.active(), pageable)
            .map(DefaultCreateLabExamTypeUseCase::toResponse);
    }
}
