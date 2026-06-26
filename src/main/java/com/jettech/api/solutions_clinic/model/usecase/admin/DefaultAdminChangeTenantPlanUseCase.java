package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAdminChangeTenantPlanUseCase implements AdminChangeTenantPlanUseCase {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void execute(AdminChangeTenantPlanRequest request) {
        var tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant não encontrado: " + request.tenantId()));

        if (tenant.getStatus() == TenantStatus.CANCELED) {
            throw new InvalidStateException(ApiError.INVALID_PLAN_CHANGE_CANCELED);
        }

        var oldPlan = tenant.getPlanType();
        tenant.setPlanType(request.planType());
        tenantRepository.save(tenant);

        var adminId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.warn("Plano alterado por admin - adminId: {}, tenantId: {}, de: {} para: {}",
                adminId, request.tenantId(), oldPlan, request.planType());
    }
}
