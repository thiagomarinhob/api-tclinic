package com.jettech.api.solutions_clinic.model.usecase.professionalschedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProfessionalScheduleResponse(
    UUID id,
    UUID professionalId,
    DayOfWeek dayOfWeek,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
