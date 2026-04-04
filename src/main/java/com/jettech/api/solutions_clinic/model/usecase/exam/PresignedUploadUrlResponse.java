package com.jettech.api.solutions_clinic.model.usecase.exam;

/**
 * Resposta com URL pré-assinada para o frontend fazer upload direto ao R2.
 */
public record PresignedUploadUrlResponse(
    String uploadUrl,
    String objectKey,
    int expiresInMinutes
) {}
