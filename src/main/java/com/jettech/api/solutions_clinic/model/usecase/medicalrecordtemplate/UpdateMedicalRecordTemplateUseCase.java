package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

public interface UpdateMedicalRecordTemplateUseCase {
    MedicalRecordTemplateResponse execute(UpdateMedicalRecordTemplateRequest request) throws AuthenticationFailedException;
}
