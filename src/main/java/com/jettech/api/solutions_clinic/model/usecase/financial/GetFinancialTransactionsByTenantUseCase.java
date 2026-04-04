package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.List;

public interface GetFinancialTransactionsByTenantUseCase extends UseCase<GetFinancialTransactionsByTenantRequest, List<FinancialTransactionResponse>> {
}
