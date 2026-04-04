package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record MedicalRecordResponse(
    UUID id,
    UUID appointmentId,
    UUID templateId,
    String patientName,
    String professionalName,
    Map<String, Object> content,
    Map<String, Object> vitalSigns,
    LocalDateTime signedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
