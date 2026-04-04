package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.Specialty;

import java.util.UUID;

public record UpdateProfessionalRequest(
    UUID id,
    Specialty specialty,
    DocumentType documentType,
    String documentNumber,
    String documentState,
    String bio
) {
}
