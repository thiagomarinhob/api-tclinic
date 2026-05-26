package com.jettech.api.solutions_clinic.model.usecase.consent;

import java.util.UUID;

public record RevokeConsentRequest(
    UUID patientId,
    UUID consentId
) {
}
