package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateLabOrderStatusRequest(
    UUID id,
    LabOrderStatus status,
    String sampleCode,
    String collectedBy,
    LocalDateTime collectedAt,
    LocalDateTime receivedAt
) {}
