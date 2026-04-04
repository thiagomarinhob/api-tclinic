package com.jettech.api.solutions_clinic.model.usecase.appointment;

import java.time.LocalDate;
import java.util.UUID;

public record GetAvailableSlotsRequest(
    UUID professionalId,
    LocalDate date,
    int durationMinutes
) {
}
