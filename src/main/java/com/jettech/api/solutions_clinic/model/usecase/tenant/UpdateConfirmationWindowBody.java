package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.Tenant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateConfirmationWindowBody(
        @NotNull(message = "A antecedência do lembrete é obrigatória")
        @Min(value = Tenant.MIN_CONFIRMATION_WINDOW_MINUTES, message = "O mínimo é 60 minutos")
        @Max(value = Tenant.MAX_CONFIRMATION_WINDOW_MINUTES, message = "O máximo é 2880 minutos")
        Integer confirmationWindowMinutes
) {
}
