package com.jettech.api.solutions_clinic.model.usecase.professionalschedule;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.List;
import java.util.UUID;

public interface GetProfessionalSchedulesByProfessionalIdUseCase extends UseCase<UUID, List<ProfessionalScheduleResponse>> {
}
