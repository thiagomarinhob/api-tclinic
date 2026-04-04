package com.jettech.api.solutions_clinic.model.usecase.laboratory;

public record GetLabExamTypesByTenantRequest(
    int page,
    int size,
    String search,
    Boolean active
) {}
