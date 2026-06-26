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
public class DefaultAdminReactivateTenantUseCase implements AdminReactivateTenantUseCase {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void execute(UUID tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant não encontrado: " + tenantId));

        if (tenant.getStatus() != TenantStatus.SUSPENDED) {
            throw new InvalidStateException(ApiError.TENANT_NOT_SUSPENDED);
        }

        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setActive(true);
        tenantRepository.save(tenant);

        var adminId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.warn("Tenant reativado por admin - adminId: {}, tenantId: {}", adminId, tenantId);
    }
}
