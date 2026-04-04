package com.jettech.api.solutions_clinic.model.usecase.room;

import java.util.UUID;

public record GetRoomsByTenantPaginatedRequest(
    UUID tenantId,
    Boolean active,
    int page,
    int size,
    String sort
) {
}
