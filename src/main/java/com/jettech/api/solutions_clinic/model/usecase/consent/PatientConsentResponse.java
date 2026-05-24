package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.model.entity.ConsentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record PatientConsentResponse(
    UUID id,
    UUID patientId,
    ConsentType consentType,
    boolean granted,
    LocalDateTime grantedAt,
    LocalDateTime revokedAt,
    String ipAddress,
    String termVersion
) {
}
