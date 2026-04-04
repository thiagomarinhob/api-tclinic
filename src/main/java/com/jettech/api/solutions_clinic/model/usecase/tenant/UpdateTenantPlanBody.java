package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import jakarta.validation.constraints.NotNull;

public record UpdateTenantPlanBody(
    @NotNull(message = "O campo [planType] é obrigatório")
    PlanType planType
) {
}
