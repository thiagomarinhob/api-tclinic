package com.jettech.api.solutions_clinic.model.usecase.procedure;

import jakarta.validation.constraints.NotNull;

public record UpdateProcedureActiveBodyRequest(
    @NotNull(message = "O campo [active] é obrigatório")
    boolean active
) {
}
