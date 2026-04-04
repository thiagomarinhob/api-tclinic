package com.jettech.api.solutions_clinic.model.usecase.room;

import java.util.UUID;

public record GetRoomsByTenantRequest(
    UUID tenantId,
    boolean activeOnly
) {
}
