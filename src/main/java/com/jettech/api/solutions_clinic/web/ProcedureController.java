package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.model.usecase.procedure.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProcedureController implements ProcedureAPI {

    private final CreateProcedureUseCase createProcedureUseCase;
    private final GetProcedureByIdUseCase getProcedureByIdUseCase;
    private final GetProceduresByTenantUseCase getProceduresByTenantUseCase;
    private final UpdateProcedureUseCase updateProcedureUseCase;
    private final UpdateProcedureActiveUseCase updateProcedureActiveUseCase;
    private final DeleteProcedureUseCase deleteProcedureUseCase;

    @Override
    public ProcedureResponse createProcedure(@Valid @RequestBody CreateProcedureRequest request) throws AuthenticationFailedException {
        return createProcedureUseCase.execute(request);
    }

    @Override
    public Page<ProcedureResponse> getProceduresByTenant(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) UUID professionalId) throws AuthenticationFailedException {
        return getProceduresByTenantUseCase.execute(new GetProceduresByTenantRequest(tenantId, page, size, sort, search, active, professionalId));
    }

    @Override
    public ProcedureResponse getProcedureById(@PathVariable UUID id) throws AuthenticationFailedException {
        return getProcedureByIdUseCase.execute(id);
    }

    @Override
    public ProcedureResponse updateProcedure(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProcedureBodyRequest request) throws AuthenticationFailedException {
        return updateProcedureUseCase.execute(new UpdateProcedureRequest(
                id,
                request.name(),
                request.description(),
                request.estimatedDurationMinutes(),
                request.basePrice(),
                request.professionalCommissionPercent()
        ));
    }

    @Override
    public ProcedureResponse updateProcedureActive(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProcedureActiveBodyRequest request) throws AuthenticationFailedException {
        return updateProcedureActiveUseCase.execute(new UpdateProcedureActiveRequest(id, request.active()));
    }

    @Override
    public void deleteProcedure(@PathVariable UUID id) throws AuthenticationFailedException {
        deleteProcedureUseCase.execute(id);
    }
}
