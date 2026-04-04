package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.healthplan.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class HealthPlanController implements HealthPlanAPI {

    private final GetHealthPlansByTenantUseCase getHealthPlansByTenantUseCase;
    private final CreateHealthPlanUseCase createHealthPlanUseCase;
    private final UpdateHealthPlanUseCase updateHealthPlanUseCase;
    private final ToggleHealthPlanActiveUseCase toggleHealthPlanActiveUseCase;

    @Override
    public List<HealthPlanResponse> getHealthPlans() throws AuthenticationFailedException {
        return getHealthPlansByTenantUseCase.execute();
    }

    @Override
    public HealthPlanResponse createHealthPlan(@Valid @RequestBody CreateHealthPlanRequest request) throws AuthenticationFailedException {
        return createHealthPlanUseCase.execute(request);
    }

    @Override
    public HealthPlanResponse updateHealthPlan(@PathVariable UUID id, @Valid @RequestBody UpdateHealthPlanRequest request) throws AuthenticationFailedException {
        return updateHealthPlanUseCase.execute(new UpdateHealthPlanRequest(id, request.name()));
    }

    @Override
    public HealthPlanResponse toggleHealthPlanActive(@PathVariable UUID id) throws AuthenticationFailedException {
        return toggleHealthPlanActiveUseCase.execute(id);
    }
}
