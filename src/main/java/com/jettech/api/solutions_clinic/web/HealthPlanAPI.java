package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.usecase.healthplan.CreateHealthPlanRequest;
import com.jettech.api.solutions_clinic.model.usecase.healthplan.HealthPlanResponse;
import com.jettech.api.solutions_clinic.model.usecase.healthplan.UpdateHealthPlanRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Convênios", description = "Gestão de convênios cadastrados pela clínica")
public interface HealthPlanAPI {

    @GetMapping("/health-plans")
    @Operation(summary = "Lista convênios da clínica")
    List<HealthPlanResponse> getHealthPlans() throws AuthenticationFailedException;

    @PostMapping("/health-plans")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra novo convênio")
    HealthPlanResponse createHealthPlan(@Valid @RequestBody CreateHealthPlanRequest request) throws AuthenticationFailedException;

    @PutMapping("/health-plans/{id}")
    @Operation(summary = "Atualiza nome do convênio")
    HealthPlanResponse updateHealthPlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHealthPlanRequest request
    ) throws AuthenticationFailedException;

    @PatchMapping("/health-plans/{id}/active")
    @Operation(summary = "Ativa ou desativa convênio")
    HealthPlanResponse toggleHealthPlanActive(@PathVariable UUID id) throws AuthenticationFailedException;
}
