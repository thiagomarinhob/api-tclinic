package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.HealthPlan;
import com.jettech.api.solutions_clinic.model.repository.HealthPlanRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateHealthPlanUseCase implements CreateHealthPlanUseCase {

    private final HealthPlanRepository healthPlanRepository;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public HealthPlanResponse execute(CreateHealthPlanRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));

        HealthPlan plan = new HealthPlan();
        plan.setTenant(tenant);
        plan.setName(request.name().trim());
        plan.setActive(true);

        plan = healthPlanRepository.save(plan);
        return new HealthPlanResponse(plan.getId(), plan.getName(), plan.isActive());
    }
}
