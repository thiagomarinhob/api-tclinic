package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.FinancialTransaction;
import com.jettech.api.solutions_clinic.model.repository.FinancialTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultGetFinancialTransactionsByTenantUseCase implements GetFinancialTransactionsByTenantUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final com.jettech.api.solutions_clinic.security.TenantContext tenantContext;

    @Override
    public List<FinancialTransactionResponse> execute(GetFinancialTransactionsByTenantRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        List<FinancialTransaction> transactions;

        if (request.startDate() != null && request.endDate() != null) {
            transactions = financialTransactionRepository.findByTenantIdAndPaymentDateBetween(
                    request.tenantId(), request.startDate(), request.endDate());
        } else if (request.type() != null && request.status() != null) {
            transactions = financialTransactionRepository.findByTenantIdAndTypeAndStatus(
                    request.tenantId(), request.type(), request.status());
        } else if (request.type() != null) {
            transactions = financialTransactionRepository.findByTenantIdAndType(request.tenantId(), request.type());
        } else if (request.status() != null) {
            transactions = financialTransactionRepository.findByTenantIdAndStatus(request.tenantId(), request.status());
        } else {
            transactions = financialTransactionRepository.findByTenantId(request.tenantId());
        }

        // Filtrar por tipo e status se fornecido (para queries que não suportam esses filtros)
        if (request.type() != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getType() == request.type())
                    .collect(Collectors.toList());
        }
        if (request.status() != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getStatus() == request.status())
                    .collect(Collectors.toList());
        }

        return transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
