package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.model.entity.ConsentType;

import java.util.UUID;

public record GetPatientConsentsRequest(
    UUID patientId,
    ConsentType consentType
) {
}
