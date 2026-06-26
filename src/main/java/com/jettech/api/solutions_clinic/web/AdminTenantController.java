package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.usecase.admin.*;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ExtendTrialBody;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ExtendTrialRequest;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ExtendTrialUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.TenantResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantPlanBody;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AdminTenantController implements AdminTenantAPI {

    private final AdminListTenantsUseCase adminListTenantsUseCase;
    private final AdminGetTenantDetailUseCase adminGetTenantDetailUseCase;
    private final AdminSuspendTenantUseCase adminSuspendTenantUseCase;
    private final AdminReactivateTenantUseCase adminReactivateTenantUseCase;
    private final AdminChangeTenantPlanUseCase adminChangeTenantPlanUseCase;
    private final ExtendTrialUseCase extendTrialUseCase;

    @Override
    public Page<AdminTenantListItemResponse> listTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TenantStatus status,
            @RequestParam(required = false) PlanType planType
    ) throws AuthenticationFailedException {
        log.info("Admin listando tenants - page: {}, size: {}, status: {}, planType: {}", page, size, status, planType);
        return adminListTenantsUseCase.execute(new AdminListTenantsRequest(page, size, status, planType));
    }

    @Override
    public AdminTenantDetailResponse getTenantDetail(@PathVariable UUID id) throws AuthenticationFailedException {
        log.info("Admin buscando detalhe do tenant - id: {}", id);
        return adminGetTenantDetailUseCase.execute(id);
    }

    @Override
    public ResponseEntity<Void> suspendTenant(@PathVariable UUID id) throws AuthenticationFailedException {
        log.warn("Admin suspendendo tenant - id: {}", id);
        adminSuspendTenantUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> reactivateTenant(@PathVariable UUID id) throws AuthenticationFailedException {
        log.warn("Admin reativando tenant - id: {}", id);
        adminReactivateTenantUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public TenantResponse extendTrial(
            @PathVariable UUID id,
            @Valid @RequestBody ExtendTrialBody body
    ) throws AuthenticationFailedException {
        log.warn("Admin estendendo trial - tenantId: {}, additionalDays: {}", id, body.additionalDays());
        return extendTrialUseCase.execute(new ExtendTrialRequest(id, body.additionalDays()));
    }

    @Override
    public ResponseEntity<Void> changeTenantPlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantPlanBody body
    ) throws AuthenticationFailedException {
        log.warn("Admin alterando plano - tenantId: {}, planType: {}", id, body.planType());
        adminChangeTenantPlanUseCase.execute(new AdminChangeTenantPlanRequest(id, body.planType()));
        return ResponseEntity.noContent().build();
    }
}
