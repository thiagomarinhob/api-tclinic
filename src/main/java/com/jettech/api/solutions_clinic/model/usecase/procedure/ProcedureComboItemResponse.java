package com.jettech.api.solutions_clinic.model.usecase.procedure;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcedureComboItemResponse(
    UUID id,
    UUID procedureId,
    String name,
    int estimatedDurationMinutes,
    BigDecimal basePrice
) {
}
