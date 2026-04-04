package com.jettech.api.solutions_clinic.model.usecase.exam;

import java.util.UUID;

public record ExamTypeResponse(
        UUID id,
        String category,
        String name
) {}
