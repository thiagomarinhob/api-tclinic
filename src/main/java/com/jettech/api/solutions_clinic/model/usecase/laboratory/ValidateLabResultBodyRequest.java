package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateLabResultBodyRequest(
    @NotNull ValidationType validationType,
    @NotBlank String validatedBy
) {
    public enum ValidationType {
        TECHNICAL, FINAL
    }
}
