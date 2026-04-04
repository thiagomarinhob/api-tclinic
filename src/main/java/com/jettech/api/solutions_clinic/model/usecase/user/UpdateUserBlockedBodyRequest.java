package com.jettech.api.solutions_clinic.model.usecase.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserBlockedBodyRequest(
    @NotNull(message = "O campo [blocked] é obrigatório")
    boolean blocked
) {
}
