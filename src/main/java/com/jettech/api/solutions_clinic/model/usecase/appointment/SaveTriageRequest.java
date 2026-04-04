package com.jettech.api.solutions_clinic.model.usecase.appointment;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaveTriageRequest(
    @NotNull UUID appointmentId,
    @NotNull JsonNode vitalSigns
) {
}
