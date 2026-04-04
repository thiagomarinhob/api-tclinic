package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;

import java.util.UUID;

public record GetLabOrdersByTenantRequest(
    int page,
    int size,
    UUID patientId,
    LabOrderStatus status,
    String search
) {}
