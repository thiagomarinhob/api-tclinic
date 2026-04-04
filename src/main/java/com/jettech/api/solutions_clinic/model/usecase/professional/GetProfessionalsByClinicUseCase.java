package com.jettech.api.solutions_clinic.model.usecase.professional;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;
import org.springframework.data.domain.Page;

public interface GetProfessionalsByClinicUseCase extends UseCase<GetProfessionalsByClinicRequest, Page<ProfessionalResponse>> {
}
