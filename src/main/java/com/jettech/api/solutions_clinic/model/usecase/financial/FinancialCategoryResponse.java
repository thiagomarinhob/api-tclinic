package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialCategoryResponse(
    UUID id,
    UUID tenantId,
    String name,
    TransactionType type,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
