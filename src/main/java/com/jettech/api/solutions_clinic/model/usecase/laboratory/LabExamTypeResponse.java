package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabSector;
import com.jettech.api.solutions_clinic.model.entity.SampleType;

import java.time.LocalDateTime;
import java.util.UUID;

public record LabExamTypeResponse(
    UUID id,
    UUID tenantId,
    String code,
    String name,
    LabSector sector,
    SampleType sampleType,
    String unit,
    String referenceRangeText,
    String preparationInfo,
    Integer turnaroundHours,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
