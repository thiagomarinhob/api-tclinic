package com.jettech.api.solutions_clinic.model.usecase.procedure;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProcedureResponse(
    UUID id,
    UUID tenantId,
    String name,
    String description,
    int estimatedDurationMinutes,
    BigDecimal basePrice,
    BigDecimal professionalCommissionPercent,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
