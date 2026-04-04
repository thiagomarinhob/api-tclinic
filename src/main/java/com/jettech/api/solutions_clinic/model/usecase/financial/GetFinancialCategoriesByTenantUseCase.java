package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.List;

public interface GetFinancialCategoriesByTenantUseCase extends UseCase<GetFinancialCategoriesByTenantRequest, List<FinancialCategoryResponse>> {
}
