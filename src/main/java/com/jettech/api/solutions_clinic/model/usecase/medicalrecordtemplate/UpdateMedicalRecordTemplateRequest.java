package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Schema como Object para evitar conflito Jackson 2/3; use case converte para JsonNode.
 */
public record UpdateMedicalRecordTemplateRequest(
    @NotNull(message = "O campo [id] é obrigatório")
    UUID id,

    @Size(min = 1, max = 255)
    String name,

    @Size(max = 50)
    String professionalType,

    Object schema,

    Boolean active
) {}
