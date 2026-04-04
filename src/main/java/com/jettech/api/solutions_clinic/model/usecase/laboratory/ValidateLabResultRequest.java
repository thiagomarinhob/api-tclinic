package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import java.util.UUID;

public record ValidateLabResultRequest(
    UUID itemId,
    ValidateLabResultBodyRequest.ValidationType validationType,
    String validatedBy
) {}
