package com.jettech.api.solutions_clinic.model.usecase.exam;

import com.jettech.api.solutions_clinic.model.entity.ExamStatus;

import java.util.UUID;

public record GetExamsByTenantRequest(
    int page,
    int size,
    UUID patientId,
    ExamStatus status
) {}
