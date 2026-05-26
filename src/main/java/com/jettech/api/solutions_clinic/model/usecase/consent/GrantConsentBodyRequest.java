package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.model.entity.ConsentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GrantConsentBodyRequest(
    @NotNull(message = "O campo [consentType] é obrigatório")
    ConsentType consentType,

    @NotBlank(message = "O campo [termVersion] é obrigatório")
    @Size(max = 10, message = "O campo [termVersion] deve ter no máximo 10 caracteres")
    String termVersion
) {
}
