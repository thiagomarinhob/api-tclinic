package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateTenantPlanRequest(
    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,
    
    @NotNull(message = "O campo [planType] é obrigatório")
    PlanType planType
) {
}
