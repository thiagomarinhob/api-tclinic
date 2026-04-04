package com.jettech.api.solutions_clinic.model.usecase.laboratory;

import jakarta.validation.constraints.NotBlank;

public record EnterLabResultBodyRequest(
    @NotBlank String resultValue,
    Boolean abnormal,
    boolean critical,
    String observations
) {}
