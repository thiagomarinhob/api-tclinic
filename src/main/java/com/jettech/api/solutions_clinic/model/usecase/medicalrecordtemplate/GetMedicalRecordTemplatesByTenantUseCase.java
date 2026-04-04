package com.jettech.api.solutions_clinic.model.usecase.medicalrecordtemplate;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.List;

public interface GetMedicalRecordTemplatesByTenantUseCase {
    List<MedicalRecordTemplateResponse> execute(GetMedicalRecordTemplatesByTenantRequest request) throws AuthenticationFailedException;
}
