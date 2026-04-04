package com.jettech.api.solutions_clinic.model.usecase.signup;

import java.util.UUID;

public record SignUpResponse(
    UUID userId,
    UUID tenantId,
    String email,
    String tenantName,
    String subdomain
) {
}

