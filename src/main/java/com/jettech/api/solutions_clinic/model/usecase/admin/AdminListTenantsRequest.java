package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;

public record AdminListTenantsRequest(
        int page,
        int size,
        TenantStatus status,
        PlanType planType
) {}
