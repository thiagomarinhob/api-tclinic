package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabResultStatus;
import com.jettech.api.solutions_clinic.model.entity.LabSector;
import com.jettech.api.solutions_clinic.model.entity.SampleType;

import java.time.LocalDateTime;
import java.util.UUID;

public record LabOrderItemResponse(
    UUID id,
    UUID examTypeId,
    String examName,
    LabSector sector,
    SampleType sampleType,
    String unit,
    String referenceRangeText,
    String resultValue,
    LabResultStatus resultStatus,
    Boolean abnormal,
    boolean critical,
    String technicalValidatedBy,
    LocalDateTime technicalValidatedAt,
    String finalValidatedBy,
    LocalDateTime finalValidatedAt,
    String observations,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
