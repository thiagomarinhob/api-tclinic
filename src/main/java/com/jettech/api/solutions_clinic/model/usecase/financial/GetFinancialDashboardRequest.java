package com.jettech.api.solutions_clinic.model.usecase.financial;

import java.time.LocalDate;
import java.util.UUID;

public record GetFinancialDashboardRequest(
    UUID tenantId,
    LocalDate startDate,
    LocalDate endDate
) {
}
