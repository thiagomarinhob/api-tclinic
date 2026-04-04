package com.jettech.api.solutions_clinic.model.usecase.exam;

import java.util.UUID;

public record GetExamsByPatientRequest(
    UUID patientId,
    int page,
    int size
) {}
