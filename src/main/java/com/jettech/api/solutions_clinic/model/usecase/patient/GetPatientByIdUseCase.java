package com.jettech.api.solutions_clinic.model.usecase.patient;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.UUID;

public interface GetPatientByIdUseCase extends UseCase<UUID, PatientResponse> {
}

