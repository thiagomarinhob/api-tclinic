package com.jettech.api.solutions_clinic.model.usecase.consent;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.List;

public interface GetPatientConsentsUseCase extends UseCase<GetPatientConsentsRequest, List<PatientConsentResponse>> {
}
