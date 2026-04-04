package com.jettech.api.solutions_clinic.model.usecase.subscription;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import jakarta.validation.constraints.NotNull;

public record CreateCheckoutSessionBody(
    @NotNull(message = "O campo [planType] é obrigatório")
    PlanType planType
) {
}
