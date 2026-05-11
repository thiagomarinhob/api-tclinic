package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.Specialty;
import jakarta.validation.constraints.NotNull;

public record UpdateProfessionalBodyRequest(
    @NotNull(message = "O campo [specialty] é obrigatório")
    Specialty specialty,

    DocumentType documentType,

    String documentNumber,

    String documentState,

    String bio
) {
}
