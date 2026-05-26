package com.jettech.api.solutions_clinic.model.usecase.procedure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateComboProcedureRequest(
    @NotBlank(message = "O campo [name] é obrigatório")
    @Size(min = 2, max = 200)
    String name,

    String description,

    @NotNull(message = "O campo [basePrice] é obrigatório")
    BigDecimal basePrice,

    UUID professionalId,

    @NotNull(message = "O campo [itemProcedureIds] é obrigatório")
    @Size(min = 2, message = "Um combo deve conter pelo menos 2 procedimentos")
    List<UUID> itemProcedureIds
) {
}
