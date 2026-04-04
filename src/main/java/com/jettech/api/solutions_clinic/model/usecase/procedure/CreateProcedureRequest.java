package com.jettech.api.solutions_clinic.model.usecase.procedure;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProcedureRequest(
    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,
    
    @NotBlank(message = "O campo [name] é obrigatório")
    @Size(min = 2, max = 200, message = "O campo [name] deve ter entre 2 e 200 caracteres")
    String name,
    
    String description,
    
    @NotNull(message = "O campo [estimatedDurationMinutes] é obrigatório")
    @Min(value = 1, message = "O campo [estimatedDurationMinutes] deve ser no mínimo 1 minuto")
    int estimatedDurationMinutes,
    
    @NotNull(message = "O campo [basePrice] é obrigatório")
    @Min(value = 0, message = "O campo [basePrice] deve ser maior ou igual a zero")
    BigDecimal basePrice,
    
    @Min(value = 0, message = "O campo [professionalCommissionPercent] deve ser maior ou igual a zero")
    BigDecimal professionalCommissionPercent,

    UUID professionalId
) {
}
