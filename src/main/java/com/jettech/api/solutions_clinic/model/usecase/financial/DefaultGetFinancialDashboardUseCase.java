package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.FinancialTransaction;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;
import com.jettech.api.solutions_clinic.model.repository.FinancialTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetFinancialDashboardUseCase implements GetFinancialDashboardUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final com.jettech.api.solutions_clinic.security.TenantContext tenantContext;

    @Override
    public FinancialDashboardResponse execute(GetFinancialDashboardRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = request.endDate() != null ? request.endDate() : LocalDate.now();

        // Calcular totais de receitas e despesas
        BigDecimal totalIncome = financialTransactionRepository.sumByTypeAndDateRange(
                request.tenantId(), TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = financialTransactionRepository.sumByTypeAndDateRange(
                request.tenantId(), TransactionType.EXPENSE, startDate, endDate);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Agrupar despesas por categoria
        List<Object[]> expensesByCategoryData = financialTransactionRepository.sumExpensesByCategory(
                request.tenantId(), startDate, endDate);
        List<FinancialDashboardResponse.CategorySummary> expensesByCategory = expensesByCategoryData.stream()
                .map(data -> new FinancialDashboardResponse.CategorySummary(
                        (String) data[0],
                        (BigDecimal) data[1]
                ))
                .collect(Collectors.toList());

        // Agrupar receitas por categoria
        List<Object[]> incomesByCategoryData = financialTransactionRepository.sumIncomesByCategory(
                request.tenantId(), startDate, endDate);
        List<FinancialDashboardResponse.CategorySummary> incomesByCategory = incomesByCategoryData.stream()
                .map(data -> new FinancialDashboardResponse.CategorySummary(
                        (String) data[0],
                        (BigDecimal) data[1]
                ))
                .collect(Collectors.toList());

        // Buscar transações pendentes
        List<FinancialTransaction> pendingTransactions = financialTransactionRepository.findPendingByTenantIdAndDueDateBetween(
                request.tenantId(), startDate, endDate.plusMonths(1)); // Próximo mês para ver pendências futuras

        List<FinancialTransactionResponse> pendingTransactionsResponse = pendingTransactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new FinancialDashboardResponse(
                request.tenantId(),
                startDate,
                endDate,
                totalIncome,
                totalExpense,
                balance,
                expensesByCategory,
                incomesByCategory,
                pendingTransactionsResponse
        );
    }

    private FinancialTransactionResponse toResponse(FinancialTransaction transaction) {
        return new FinancialTransactionResponse(
                transaction.getId(),
                transaction.getTenant().getId(),
                transaction.getDescription(),
                transaction.getType(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getCategory() != null ? transaction.getCategory().getName() : null,
                transaction.getAmount(),
                transaction.getDueDate(),
                transaction.getPaymentDate(),
                transaction.getStatus(),
                transaction.getPaymentMethod(),
                transaction.getAppointment() != null ? transaction.getAppointment().getId() : null,
                transaction.getProfessional() != null ? transaction.getProfessional().getId() : null,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
