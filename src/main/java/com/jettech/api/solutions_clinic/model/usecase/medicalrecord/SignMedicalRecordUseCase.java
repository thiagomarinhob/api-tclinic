package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

public interface SignMedicalRecordUseCase {
    MedicalRecordResponse execute(UUID recordId) throws AuthenticationFailedException;
}
