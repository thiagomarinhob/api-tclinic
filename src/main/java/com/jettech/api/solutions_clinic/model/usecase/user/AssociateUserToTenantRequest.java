package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.Role;

import java.util.UUID;

public record AssociateUserToTenantRequest(
    UUID userId,
    UUID tenantId,
    Role role
) {
}
