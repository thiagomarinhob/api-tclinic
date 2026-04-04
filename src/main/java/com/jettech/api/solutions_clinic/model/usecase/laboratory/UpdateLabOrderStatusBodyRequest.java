package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateLabOrderStatusBodyRequest(
    @NotNull LabOrderStatus status,
    String sampleCode,
    String collectedBy,
    LocalDateTime collectedAt,
    LocalDateTime receivedAt
) {}
