package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabSector;
import com.jettech.api.solutions_clinic.model.entity.SampleType;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateLabOrderItemRequest(
    UUID examTypeId,
    @NotBlank String examName,
    LabSector sector,
    SampleType sampleType,
    String unit,
    String referenceRangeText
) {}
