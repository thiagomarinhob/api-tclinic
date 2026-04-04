package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;

import java.time.LocalDate;
import java.util.UUID;

public record GetFinancialTransactionsByTenantRequest(
    UUID tenantId,
    TransactionType type,
    PaymentStatus status,
    LocalDate startDate,
    LocalDate endDate
) {
}
