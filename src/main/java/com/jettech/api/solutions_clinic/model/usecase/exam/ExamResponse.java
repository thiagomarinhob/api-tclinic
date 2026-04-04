package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.ExamStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExamResponse(
    UUID id,
    UUID tenantId,
    UUID patientId,
    UUID appointmentId,
    String name,
    String clinicalIndication,
    ExamStatus status,
    String resultFileKey,
    String requestFileKey,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
