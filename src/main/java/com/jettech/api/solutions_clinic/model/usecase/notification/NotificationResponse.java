package com.jettech.api.solutions_clinic.model.usecase.notification;

import com.jettech.api.solutions_clinic.model.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID tenantId,
    NotificationType type,
    String title,
    String description,
    boolean read,
    String referenceType,
    UUID referenceId,
    LocalDateTime createdAt
) {
}
