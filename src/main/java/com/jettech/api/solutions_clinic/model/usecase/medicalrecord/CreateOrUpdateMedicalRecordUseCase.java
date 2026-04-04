package com.jettech.api.solutions_clinic.model.usecase.medicalrecord;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

public interface CreateOrUpdateMedicalRecordUseCase {
    MedicalRecordResponse execute(CreateOrUpdateMedicalRecordRequest request) throws AuthenticationFailedException;
}
