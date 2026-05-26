package com.jettech.api.solutions_clinic.model.usecase.appointment;

import java.util.UUID;

public record CancelAppointmentRequest(UUID id, String reason) {
}
