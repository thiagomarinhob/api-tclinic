package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAdminSuspendTenantUseCase implements AdminSuspendTenantUseCase {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void execute(UUID tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant não encontrado: " + tenantId));

        switch (tenant.getStatus()) {
            case SUSPENDED -> throw new InvalidStateException(ApiError.TENANT_ALREADY_SUSPENDED);
            case CANCELED, PENDING_SETUP -> throw new InvalidStateException(ApiError.INVALID_TRANSITION_SUSPEND);
            default -> { /* ACTIVE or TRIAL — proceed */ }
        }

        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setActive(false);
        tenantRepository.save(tenant);

        var adminId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.warn("Tenant suspenso por admin - adminId: {}, tenantId: {}", adminId, tenantId);
    }
}
