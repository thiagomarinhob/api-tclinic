package com.jettech.api.solutions_clinic.model.usecase.procedure;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProcedureRequest(
    UUID id,
    String name,
    String description,
    Integer estimatedDurationMinutes,
    BigDecimal basePrice,
    BigDecimal professionalCommissionPercent
) {
}
