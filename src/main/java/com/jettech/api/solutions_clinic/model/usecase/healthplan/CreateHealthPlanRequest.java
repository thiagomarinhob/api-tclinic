package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import jakarta.validation.constraints.NotBlank;

public record CreateHealthPlanRequest(
        @NotBlank String name
) {}
