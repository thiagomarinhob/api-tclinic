package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.repository.HealthPlanRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetHealthPlansByTenantUseCase implements GetHealthPlansByTenantUseCase {

    private final HealthPlanRepository healthPlanRepository;
    private final TenantContext tenantContext;

    @Override
    public List<HealthPlanResponse> execute() throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        return healthPlanRepository.findByTenantIdOrderByNameAsc(tenantId)
                .stream()
                .map(h -> new HealthPlanResponse(h.getId(), h.getName(), h.isActive()))
                .toList();
    }
}
