package com.jettech.api.solutions_clinic.model.usecase.attachment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentResponse(
    UUID id,
    UUID appointmentId,
    String fileName,
    String fileType,
    Long fileSizeBytes,
    LocalDateTime createdAt
) {}
