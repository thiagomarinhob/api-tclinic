package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import com.jettech.api.solutions_clinic.model.entity.LabPaymentType;
import com.jettech.api.solutions_clinic.model.entity.LabPriority;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateLabOrderBodyRequest(
    @NotNull UUID patientId,
    UUID appointmentId,
    UUID professionalId,
    String requesterName,
    LabPriority priority,
    LabPaymentType paymentType,
    UUID healthPlanId,
    String clinicalNotes,
    @NotEmpty List<CreateLabOrderItemRequest> items
) {}
