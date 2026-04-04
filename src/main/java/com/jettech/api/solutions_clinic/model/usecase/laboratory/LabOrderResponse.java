package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabOrderStatus;
import com.jettech.api.solutions_clinic.model.entity.LabPaymentType;
import com.jettech.api.solutions_clinic.model.entity.LabPriority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LabOrderResponse(
    UUID id,
    UUID tenantId,
    UUID patientId,
    String patientName,
    UUID appointmentId,
    UUID professionalId,
    String requesterName,
    LabPriority priority,
    LabPaymentType paymentType,
    UUID healthPlanId,
    String healthPlanName,
    String clinicalNotes,
    LabOrderStatus status,
    String sampleCode,
    LocalDateTime collectedAt,
    String collectedBy,
    LocalDateTime receivedAt,
    List<LabOrderItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
