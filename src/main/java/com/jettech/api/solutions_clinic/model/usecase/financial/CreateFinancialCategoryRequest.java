package com.jettech.api.solutions_clinic.model.usecase.financial;

import com.jettech.api.solutions_clinic.model.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateFinancialCategoryRequest(
    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,
    
    @NotBlank(message = "O campo [name] é obrigatório")
    @Size(min = 2, max = 200, message = "O campo [name] deve ter entre 2 e 200 caracteres")
    String name,
    
    @NotNull(message = "O campo [type] é obrigatório")
    TransactionType type
) {
}
