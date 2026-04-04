package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.TransactionType;

import java.util.UUID;

public record GetFinancialCategoriesByTenantRequest(
    UUID tenantId,
    TransactionType type,
    Boolean active
) {
}
