package com.jettech.api.solutions_clinic.model.usecase.procedure;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProcedureBodyRequest(
    String name,
    String description,
    @Min(value = 1, message = "O campo [estimatedDurationMinutes] deve ser no mínimo 1 minuto")
    Integer estimatedDurationMinutes,
    @Min(value = 0, message = "O campo [basePrice] deve ser maior ou igual a zero")
    BigDecimal basePrice,
    @Min(value = 0, message = "O campo [professionalCommissionPercent] deve ser maior ou igual a zero")
    BigDecimal professionalCommissionPercent
) {
}
