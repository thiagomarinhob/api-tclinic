package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.ExamStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** Resposta de exame na listagem, com nome do paciente para exibição. */
public record ExamListResponse(
    UUID id,
    UUID tenantId,
    UUID patientId,
    String patientFirstName,
    UUID appointmentId,
    String name,
    String clinicalIndication,
    ExamStatus status,
    String resultFileKey,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
