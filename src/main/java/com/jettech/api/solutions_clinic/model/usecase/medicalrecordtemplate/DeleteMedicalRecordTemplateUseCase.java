package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

public interface DeleteMedicalRecordTemplateUseCase {
    void execute(UUID id) throws AuthenticationFailedException;
}
