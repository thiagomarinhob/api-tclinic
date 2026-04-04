package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalRecordTemplateResponse(
    UUID id,
    UUID tenantId,       // null = template global (sistema)
    UUID professionalId, // null = modelo da clínica (todos); preenchido = modelo exclusivo do profissional
    String name,
    String professionalType,
    @JsonRawValue String schema,
    boolean readOnly,   // true = template padrão do sistema, não editável/apagável pela clínica
    boolean active,
    boolean defaultTemplate, // true = modelo pré-selecionado ao abrir novo prontuário (um por tenant)
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
