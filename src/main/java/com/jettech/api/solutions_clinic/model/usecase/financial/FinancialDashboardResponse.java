package com.jettech.api.solutions_clinic.model.usecase.financial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record FinancialDashboardResponse(
    UUID tenantId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance,
    List<CategorySummary> expensesByCategory,
    List<CategorySummary> incomesByCategory,
    List<FinancialTransactionResponse> pendingTransactions
) {
    public record CategorySummary(
        String categoryName,
        BigDecimal totalAmount
    ) {
    }
}
