package com.jettech.api.solutions_clinic.model.usecase.tenant;

import jakarta.validation.constraints.NotBlank;

public record UpdateTenantLogoBody(
    @NotBlank String objectKey
) {
}
