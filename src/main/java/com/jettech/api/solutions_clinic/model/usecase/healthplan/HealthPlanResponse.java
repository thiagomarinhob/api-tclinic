package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import java.util.UUID;

public record HealthPlanResponse(
        UUID id,
        String name,
        boolean active
) {}
