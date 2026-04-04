package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.FinancialCategory;
import com.jettech.api.solutions_clinic.model.repository.FinancialCategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetFinancialCategoriesByTenantUseCase implements GetFinancialCategoriesByTenantUseCase {

    private final FinancialCategoryRepository financialCategoryRepository;
    private final com.jettech.api.solutions_clinic.security.TenantContext tenantContext;

    @Override
    public List<FinancialCategoryResponse> execute(GetFinancialCategoriesByTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        List<FinancialCategory> categories;

        if (request.type() != null && request.active() != null) {
            categories = financialCategoryRepository.findByTenantIdAndTypeAndActive(
                    request.tenantId(), request.type(), request.active());
        } else if (request.type() != null) {
            categories = financialCategoryRepository.findByTenantIdAndType(request.tenantId(), request.type());
        } else if (request.active() != null) {
            categories = financialCategoryRepository.findByTenantIdAndActive(request.tenantId(), request.active());
        } else {
            categories = financialCategoryRepository.findByTenantId(request.tenantId());
        }

        return categories.stream()
                .map(category -> new FinancialCategoryResponse(
                        category.getId(),
                        category.getTenant().getId(),
                        category.getName(),
                        category.getType(),
                        category.isActive(),
                        category.getCreatedAt(),
                        category.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}
