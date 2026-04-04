package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import jakarta.validation.constraints.Size;

/**
 * Body do PUT /medical-record-templates/{id}. Id vem do path.
 * Schema como Object para evitar conflito Jackson 2/3; use case converte para JsonNode.
 */
public record UpdateMedicalRecordTemplateBody(
    @Size(min = 1, max = 255)
    String name,

    @Size(max = 50)
    String professionalType,

    Object schema,

    Boolean active
) {}
