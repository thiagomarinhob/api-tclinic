package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import java.util.UUID;

public record EnterLabResultRequest(
    UUID itemId,
    String resultValue,
    Boolean abnormal,
    boolean critical,
    String observations
) {}
