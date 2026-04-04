package com.jettech.api.solutions_clinic.model.usecase.attachment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmAttachmentUploadRequest(
    @NotNull UUID appointmentId,
    @NotBlank String objectKey,
    @NotBlank String fileName,
    String fileType,
    Long fileSizeBytes
) {}
