package com.jettech.api.solutions_clinic.model.usecase.professional;

import java.util.UUID;

public record UpdateProfessionalActiveRequest(
    UUID id,
    boolean active
) {
}
