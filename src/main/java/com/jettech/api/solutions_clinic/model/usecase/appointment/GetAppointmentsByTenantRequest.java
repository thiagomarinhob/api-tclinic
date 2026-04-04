package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record GetAppointmentsByTenantRequest(
    UUID tenantId,
    LocalDate date,
    LocalDate startDate,
    LocalDate endDate,
    AppointmentStatus status,
    String orderBy // scheduledAt, createdAt, patientName, professionalName
) {
}
