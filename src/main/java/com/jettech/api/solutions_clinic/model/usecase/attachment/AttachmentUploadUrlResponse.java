package com.jettech.api.solutions_clinic.model.usecase.attachment;

public record AttachmentUploadUrlResponse(
    String uploadUrl,
    String objectKey,
    int expiresInMinutes
) {}
