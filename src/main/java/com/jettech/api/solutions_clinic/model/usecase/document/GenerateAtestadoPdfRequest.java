package com.jettech.api.solutions_clinic.model.usecase.document;

import jakarta.validation.constraints.Min;

public record GenerateAtestadoPdfRequest(
    @Min(1) int dias,
    String motivo
) {}
