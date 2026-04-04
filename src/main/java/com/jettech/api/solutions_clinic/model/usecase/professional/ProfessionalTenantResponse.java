package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.Specialty;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.entity.TypeTenant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProfessionalTenantResponse(
    List<TenantInfo> tenants
) {
    public record TenantInfo(
        UUID tenantId,
        String tenantName,
        String cnpj,
        String subdomain,
        TypeTenant type,
        TenantStatus status,
        PlanType planType,
        LocalDate trialEndsAt,
        boolean active,
        UUID professionalId,
        Specialty specialty,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }
}

