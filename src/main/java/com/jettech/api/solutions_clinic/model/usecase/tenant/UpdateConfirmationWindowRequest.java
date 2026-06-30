package com.jettech.api.solutions_clinic.model.usecase.tenant;

import java.util.UUID;

public record UpdateConfirmationWindowRequest(
        UUID tenantId,
        int confirmationWindowMinutes
) {
}
