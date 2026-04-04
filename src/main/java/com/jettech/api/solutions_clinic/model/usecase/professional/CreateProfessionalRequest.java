package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.DocumentType;
import com.jettech.api.solutions_clinic.model.entity.Specialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProfessionalRequest(
    @NotNull(message = "O campo [userId] é obrigatório")
    UUID userId,

    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,

    @NotNull(message = "O campo [specialty] é obrigatório")
    Specialty specialty,
    
    @NotNull(message = "O campo [documentType] é obrigatório")
    DocumentType documentType,
    
    @NotBlank(message = "O campo [documentNumber] é obrigatório")
    String documentNumber,
    
    String documentState,
    
    String bio
) {
}

