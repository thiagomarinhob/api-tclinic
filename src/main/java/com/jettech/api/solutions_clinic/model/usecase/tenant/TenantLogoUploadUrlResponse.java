package com.jettech.api.solutions_clinic.model.usecase.tenant;

public record TenantLogoUploadUrlResponse(
    String uploadUrl,
    String objectKey,
    int expiresInMinutes
) {
}
