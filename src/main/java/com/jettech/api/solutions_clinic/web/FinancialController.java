package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;
import com.jettech.api.solutions_clinic.model.usecase.financial.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class FinancialController implements FinancialAPI {

    private final CreateFinancialCategoryUseCase createFinancialCategoryUseCase;
    private final GetFinancialCategoriesByTenantUseCase getFinancialCategoriesByTenantUseCase;
    private final CreateFinancialTransactionUseCase createFinancialTransactionUseCase;
    private final GetFinancialTransactionsByTenantUseCase getFinancialTransactionsByTenantUseCase;
    private final GetFinancialDashboardUseCase getFinancialDashboardUseCase;

    @Override
    public FinancialCategoryResponse createFinancialCategory(@Valid @RequestBody CreateFinancialCategoryRequest request) throws AuthenticationFailedException {
        return createFinancialCategoryUseCase.execute(request);
    }

    @Override
    public List<FinancialCategoryResponse> getFinancialCategoriesByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Boolean active
    ) throws AuthenticationFailedException {
        return getFinancialCategoriesByTenantUseCase.execute(new GetFinancialCategoriesByTenantRequest(tenantId, type, active));
    }

    @Override
    public FinancialTransactionResponse createFinancialTransaction(@Valid @RequestBody CreateFinancialTransactionRequest request) throws AuthenticationFailedException {
        return createFinancialTransactionUseCase.execute(request);
    }

    @Override
    public List<FinancialTransactionResponse> getFinancialTransactionsByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws AuthenticationFailedException {
        return getFinancialTransactionsByTenantUseCase.execute(
                new GetFinancialTransactionsByTenantRequest(tenantId, type, status, startDate, endDate));
    }

    @Override
    public FinancialDashboardResponse getFinancialDashboard(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws AuthenticationFailedException {
        return getFinancialDashboardUseCase.execute(new GetFinancialDashboardRequest(tenantId, startDate, endDate));
    }
}
