package com.jettech.api.solutions_clinic.model.usecase.appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckAvailabilityRequest(
    UUID professionalId,
    LocalDateTime startTime,
    int durationMinutes,
    UUID excludeAppointmentId
) {
}
