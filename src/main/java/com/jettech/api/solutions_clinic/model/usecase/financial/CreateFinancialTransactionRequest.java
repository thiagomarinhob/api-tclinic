package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.PaymentMethod;
import com.jettech.api.solutions_clinic.model.entity.PaymentStatus;
import com.jettech.api.solutions_clinic.model.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateFinancialTransactionRequest(
    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,
    
    @NotBlank(message = "O campo [description] é obrigatório")
    @Size(min = 2, max = 500, message = "O campo [description] deve ter entre 2 e 500 caracteres")
    String description,
    
    @NotNull(message = "O campo [type] é obrigatório")
    TransactionType type,
    
    UUID categoryId,
    
    @NotNull(message = "O campo [amount] é obrigatório")
    BigDecimal amount,
    
    @NotNull(message = "O campo [dueDate] é obrigatório")
    LocalDate dueDate,
    
    LocalDate paymentDate,
    
    @NotNull(message = "O campo [status] é obrigatório")
    PaymentStatus status,
    
    PaymentMethod paymentMethod,
    
    UUID appointmentId,
    
    UUID professionalId
) {
}
