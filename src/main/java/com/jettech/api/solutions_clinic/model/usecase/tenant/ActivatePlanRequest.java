package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.PlanType;

import java.util.UUID;

public record ActivatePlanRequest(
        UUID tenantId,
        PlanType planType
) {
}
