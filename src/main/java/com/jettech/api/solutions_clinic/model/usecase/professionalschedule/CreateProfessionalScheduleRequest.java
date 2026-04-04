package com.jettech.api.solutions_clinic.model.usecase.professionalschedule;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.UUID;

public record CreateProfessionalScheduleRequest(
    @NotNull(message = "O campo [professionalId] é obrigatório")
    UUID professionalId,

    @NotNull(message = "O campo [dayOfWeek] é obrigatório")
    DayOfWeek dayOfWeek
) {
}
