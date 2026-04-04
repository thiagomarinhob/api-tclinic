package com.jettech.api.solutions_clinic.model.usecase.patient;

import jakarta.validation.constraints.NotNull;

public record UpdatePatientActiveBodyRequest(
    @NotNull(message = "O campo [active] é obrigatório")
    boolean active
) {
}
