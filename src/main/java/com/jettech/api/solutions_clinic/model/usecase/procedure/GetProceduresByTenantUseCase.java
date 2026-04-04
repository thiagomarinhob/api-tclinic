package com.jettech.api.solutions_clinic.model.usecase.procedure;

import com.jettech.api.solutions_clinic.model.usecase.UseCase;
import org.springframework.data.domain.Page;

public interface GetProceduresByTenantUseCase extends UseCase<GetProceduresByTenantRequest, Page<ProcedureResponse>> {
}
