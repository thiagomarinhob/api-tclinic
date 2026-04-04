package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.Specialty;
import jakarta.validation.constraints.NotNull;

public record UpdateProfessionalBodyRequest(
    @NotNull(message = "O campo [specialty] é obrigatório")
    Specialty specialty,

    @NotNull(message = "O campo [documentType] é obrigatório")
    DocumentType documentType,

    @NotNull(message = "O campo [documentNumber] é obrigatório")
    String documentNumber,

    String documentState,

    String bio
) {
}
