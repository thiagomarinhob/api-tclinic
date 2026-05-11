package com.jettech.api.solutions_clinic.model.usecase.tenant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExtendTrialBody(
        @NotNull(message = "A quantidade de dias é obrigatória")
        @Min(value = 1, message = "O mínimo é 1 dia")
        @Max(value = 365, message = "O máximo é 365 dias por extensão")
        Integer additionalDays
) {
}
