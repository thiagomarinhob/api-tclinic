package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

public interface GetMedicalRecordTemplateByIdUseCase {
    MedicalRecordTemplateResponse execute(UUID id) throws AuthenticationFailedException;
}
