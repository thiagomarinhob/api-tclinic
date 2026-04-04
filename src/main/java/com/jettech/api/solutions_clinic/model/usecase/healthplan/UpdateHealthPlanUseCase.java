package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

public interface UpdateHealthPlanUseCase {
    HealthPlanResponse execute(UpdateHealthPlanRequest request) throws AuthenticationFailedException;
}
