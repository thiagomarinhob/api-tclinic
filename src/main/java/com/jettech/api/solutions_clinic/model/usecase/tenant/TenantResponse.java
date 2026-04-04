package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.entity.TypeTenant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
    UUID id,
    String name,
    String cnpj,
    PlanType planType,
    String address,
    String phone,
    boolean active,
    String subdomain,
    TypeTenant type,
    TenantStatus status,
    LocalDate trialEndsAt,
    String logoObjectKey,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
