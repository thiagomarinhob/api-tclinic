package com.jettech.api.solutions_clinic.model.usecase.room;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomResponse(
    UUID id,
    UUID tenantId,
    String name,
    String description,
    Integer capacity,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

