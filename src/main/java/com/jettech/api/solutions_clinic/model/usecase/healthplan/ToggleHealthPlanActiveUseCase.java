package com.jettech.api.solutions_clinic.model.usecase.healthplan;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;

import java.util.UUID;

public interface ToggleHealthPlanActiveUseCase {
    HealthPlanResponse execute(UUID id) throws AuthenticationFailedException;
}
