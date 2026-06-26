package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.model.entity.PlanType;

import java.util.UUID;

public record AdminChangeTenantPlanRequest(UUID tenantId, PlanType planType) {}
