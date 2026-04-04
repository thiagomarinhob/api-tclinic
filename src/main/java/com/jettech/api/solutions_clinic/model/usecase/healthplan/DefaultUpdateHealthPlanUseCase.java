package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.repository.HealthPlanRepository;
import com.jettech.api.solutions_clinic.security.TenantContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateHealthPlanUseCase implements UpdateHealthPlanUseCase {

    private final HealthPlanRepository healthPlanRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public HealthPlanResponse execute(UpdateHealthPlanRequest request) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        var plan = healthPlanRepository.findByIdAndTenantId(request.id(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Convênio", request.id()));

        plan.setName(request.name().trim());
        plan = healthPlanRepository.save(plan);
        return new HealthPlanResponse(plan.getId(), plan.getName(), plan.isActive());
    }
}
