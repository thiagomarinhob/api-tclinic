package com.jettech.api.solutions_clinic.model.usecase.procedure;

import java.util.UUID;

public record GetProceduresByTenantRequest(
    UUID tenantId,
    int page,
    int size,
    String sort,
    String search,
    Boolean active,
    UUID professionalId
) {
}
