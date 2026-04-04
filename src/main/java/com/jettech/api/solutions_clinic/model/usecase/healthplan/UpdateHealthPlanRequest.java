package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateHealthPlanRequest(
        UUID id,
        @NotBlank String name
) {}
