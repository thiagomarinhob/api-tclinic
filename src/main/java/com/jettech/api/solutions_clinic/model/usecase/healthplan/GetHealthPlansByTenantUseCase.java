package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.List;

public interface GetHealthPlansByTenantUseCase {
    List<HealthPlanResponse> execute() throws AuthenticationFailedException;
}
