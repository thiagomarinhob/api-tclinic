package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.PaymentMethod;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialTransactionResponse(
    UUID id,
    UUID tenantId,
    String description,
    TransactionType type,
    UUID categoryId,
    String categoryName,
    BigDecimal amount,
    LocalDate dueDate,
    LocalDate paymentDate,
    PaymentStatus status,
    PaymentMethod paymentMethod,
    UUID appointmentId,
    UUID professionalId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
