package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.entity.TypeTenant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminTenantDetailResponse(
        UUID id,
        String name,
        String cnpj,
        String subdomain,
        String address,
        String phone,
        TypeTenant type,
        TenantStatus status,
        PlanType planType,
        LocalDate trialEndsAt,
        String logoObjectKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AdminOwnerResponse owner,
        AdminSubscriptionDetailResponse subscription
) {}
