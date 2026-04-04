package com.jettech.api.solutions_clinic.model.usecase.patient;

import java.util.UUID;

public record UpdatePatientActiveRequest(
    UUID id,
    boolean active
) {
}
