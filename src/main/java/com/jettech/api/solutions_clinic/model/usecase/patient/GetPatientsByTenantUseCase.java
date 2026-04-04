package com.jettech.api.solutions_clinic.model.usecase.patient;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;
import org.springframework.data.domain.Page;

public interface GetPatientsByTenantUseCase extends UseCase<GetPatientsByTenantRequest, Page<PatientResponse>> {
}
