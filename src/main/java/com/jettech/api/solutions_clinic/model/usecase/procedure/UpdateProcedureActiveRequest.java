package com.jettech.api.solutions_clinic.model.usecase.procedure;

import java.util.UUID;

public record UpdateProcedureActiveRequest(
    UUID id,
    boolean active
) {
}
