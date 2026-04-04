package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateOrUpdateMedicalRecordRequest(
    @NotNull(message = "O campo [appointmentId] é obrigatório")
    UUID appointmentId,

    @NotNull(message = "O campo [templateId] é obrigatório")
    UUID templateId,

    @NotNull(message = "O campo [content] é obrigatório")
    Map<String, Object> content,

    Map<String, Object> vitalSigns
) {}
