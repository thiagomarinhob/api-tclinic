package com.jettech.api.solutions_clinic.model.usecase.laboratory;

public record LabDashboardResponse(
    long totalRequested,
    long totalCollected,
    long totalInAnalysis,
    long totalCompleted,
    long pendingResults,
    long awaitingValidation
) {}
