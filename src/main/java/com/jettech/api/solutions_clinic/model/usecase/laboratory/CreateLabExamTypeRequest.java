package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabSector;
import com.jettech.api.solutions_clinic.model.entity.SampleType;
import jakarta.validation.constraints.NotBlank;

public record CreateLabExamTypeRequest(
    String code,
    @NotBlank String name,
    LabSector sector,
    SampleType sampleType,
    String unit,
    String referenceRangeText,
    String preparationInfo,
    Integer turnaroundHours
) {}
