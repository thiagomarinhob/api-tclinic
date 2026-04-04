package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;

import java.util.UUID;

public interface GetProcedureByIdUseCase extends UseCase<UUID, ProcedureResponse> {
}
