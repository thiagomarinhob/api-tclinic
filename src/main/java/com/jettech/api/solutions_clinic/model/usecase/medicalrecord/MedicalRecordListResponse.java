package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Item da listagem de prontuários (para tabela com paciente, profissional, datas).
 */
public record MedicalRecordListResponse(
    UUID id,
    UUID appointmentId,
    String patientName,
    String professionalName,
    LocalDateTime scheduledAt,
    LocalDateTime createdAt,
    LocalDateTime signedAt
) {}
