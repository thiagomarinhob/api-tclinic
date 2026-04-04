package com.jettech.api.solutions_clinic.model.usecase.professional;

import jakarta.validation.constraints.NotNull;

public record UpdateProfessionalActiveBodyRequest(
    @NotNull(message = "O campo [active] é obrigatório")
    boolean active
) {
}
