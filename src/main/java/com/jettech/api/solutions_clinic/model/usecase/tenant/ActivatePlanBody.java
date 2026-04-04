package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import jakarta.validation.constraints.NotNull;

public record ActivatePlanBody(
        @NotNull(message = "O tipo de plano é obrigatório")
        PlanType planType
) {
}
