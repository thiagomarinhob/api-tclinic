package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.repository.LabOrderRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetLabOrdersByTenantUseCase implements GetLabOrdersByTenantUseCase {

    private final LabOrderRepository labOrderRepository;
    private final TenantContext tenantContext;

    @Override
    public Page<LabOrderResponse> execute(GetLabOrdersByTenantRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        PageRequest pageable = PageRequest.of(request.page(), request.size());
        return labOrderRepository.findByTenantWithFilters(
            tenantId, request.patientId(), request.status(), request.search(), pageable
        ).map(DefaultCreateLabOrderUseCase::toResponse);
    }
}
