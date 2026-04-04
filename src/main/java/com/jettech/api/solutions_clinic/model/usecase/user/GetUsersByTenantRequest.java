package com.jettech.api.solutions_clinic.model.usecase.user;

import com.jettech.api.solutions_clinic.model.entity.Role;

import java.util.UUID;

public record GetUsersByTenantRequest(
    UUID tenantId,
    int page,
    int size,
    String sort,
    String search,
    Boolean blocked,
    Role role
) {
}
