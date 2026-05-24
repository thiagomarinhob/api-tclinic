package com.jettech.api.solutions_clinic.model.usecase.consent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TreatmentConsentRequest(
    @NotNull(message = "O campo [treatmentConsent.granted] é obrigatório")
    Boolean granted,

    @NotBlank(message = "O campo [treatmentConsent.termVersion] é obrigatório")
    @Size(max = 10, message = "O campo [treatmentConsent.termVersion] deve ter no máximo 10 caracteres")
    String termVersion
) {
}
