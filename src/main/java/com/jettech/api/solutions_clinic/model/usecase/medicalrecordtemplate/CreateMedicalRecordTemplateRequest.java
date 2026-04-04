package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Schema é recebido como Object (array/objeto) para evitar conflito de deserialização
 * entre Jackson 2 (com.fasterxml) e Jackson 3 (tools.jackson). O use case converte para JsonNode.
 */
public record CreateMedicalRecordTemplateRequest(
    @NotNull(message = "O campo [tenantId] é obrigatório")
    UUID tenantId,

    /** Se preenchido, modelo exclusivo desse profissional; null = modelo da clínica (todos). */
    UUID professionalId,

    @NotBlank(message = "O campo [name] é obrigatório")
    @Size(min = 1, max = 255)
    String name,

    @Size(max = 50)
    String professionalType,

    @NotNull(message = "O campo [schema] é obrigatório")
    Object schema
) {}
