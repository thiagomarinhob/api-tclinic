package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidRequestException;
import com.jettech.api.solutions_clinic.model.entity.Role;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.jettech.api.solutions_clinic.model.usecase.subscription.CreateCheckoutSessionBody;
import com.jettech.api.solutions_clinic.model.usecase.subscription.CreateCheckoutSessionRequest;
import com.jettech.api.solutions_clinic.model.usecase.subscription.CreateCheckoutSessionResponse;
import com.jettech.api.solutions_clinic.model.usecase.subscription.CreateCheckoutSessionUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ActivatePlanBody;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ActivatePlanRequest;
import com.jettech.api.solutions_clinic.model.usecase.tenant.ActivatePlanUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.StartTrialUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantPlanUseCase;
import com.jettech.api.solutions_clinic.model.usecase.tenant.StartTrialRequest;
import com.jettech.api.solutions_clinic.model.usecase.tenant.TenantLogoUploadUrlResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.TenantResponse;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantLogoBody;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantPlanBody;
import com.jettech.api.solutions_clinic.model.usecase.tenant.UpdateTenantPlanRequest;
import com.jettech.api.solutions_clinic.security.TenantContext;
import com.jettech.api.solutions_clinic.model.usecase.user.AssociateUserToTenantRequest;
import com.jettech.api.solutions_clinic.model.usecase.user.AssociateUserToTenantUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TenantController implements TenantAPI {

    private final UpdateTenantPlanUseCase updateTenantPlanUseCase;
    private final CreateCheckoutSessionUseCase createCheckoutSessionUseCase;
    private final AssociateUserToTenantUseCase associateUserToTenantUseCase;
    private final ActivatePlanUseCase activatePlanUseCase;
    private final StartTrialUseCase startTrialUseCase;
    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;
    private final R2StorageService r2StorageService;

    @Override
    public TenantResponse updateTenantPlan(
            @PathVariable UUID tenantId,
            @Valid @RequestBody UpdateTenantPlanBody body
    ) throws AuthenticationFailedException {
        log.info("Recebendo atualização de plano - tenantId: {}, planType: {}", tenantId, body.planType());
        UpdateTenantPlanRequest request = new UpdateTenantPlanRequest(tenantId, body.planType());
        return updateTenantPlanUseCase.execute(request);
    }

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateCheckoutSessionBody body
    ) throws AuthenticationFailedException {
        log.info("Criando sessão de checkout - tenantId: {}, planType: {}", tenantId, body.planType());
        CreateCheckoutSessionRequest request = new CreateCheckoutSessionRequest(tenantId, body.planType());
        return createCheckoutSessionUseCase.execute(request);
    }

    @Override
    public void associateUserToTenant(
            @PathVariable UUID tenantId,
            @PathVariable UUID userId,
            @PathVariable String role
    ) throws AuthenticationFailedException {
        log.info("Associando usuário à clínica - tenantId: {}, userId: {}, role: {}", tenantId, userId, role);
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(ApiError.INVALID_ROLE);
        }
        AssociateUserToTenantRequest request = new AssociateUserToTenantRequest(userId, tenantId, roleEnum);
        associateUserToTenantUseCase.execute(request);
    }

    @Override
    public TenantResponse activatePlan(
            @PathVariable UUID tenantId,
            @Valid @RequestBody ActivatePlanBody body
    ) throws AuthenticationFailedException {
        log.warn("ATIVACAO MANUAL - tenantId: {}, planType: {} - USE APENAS PARA TESTES!", tenantId, body.planType());
        ActivatePlanRequest request = new ActivatePlanRequest(tenantId, body.planType());
        return activatePlanUseCase.execute(request);
    }

    @Override
    public TenantResponse startTrial(
            @PathVariable UUID tenantId
    ) throws AuthenticationFailedException {
        log.info("Iniciando trial - tenantId: {}", tenantId);
        StartTrialRequest request = new StartTrialRequest(tenantId);
        return startTrialUseCase.execute(request);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant() throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));
        return toResponse(tenant);
    }

    @Override
    public TenantLogoUploadUrlResponse getLogoUploadUrl(
            @RequestParam(required = false) String fileName
    ) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        String ext = resolveExtension(fileName);
        String objectKey = "tenants/" + tenantId + "/logo/logo" + ext;
        String uploadUrl = r2StorageService.createPresignedPutUrl(objectKey, 10);
        if (uploadUrl == null) {
            throw new InvalidRequestException(ApiError.R2_NOT_CONFIGURED);
        }
        return new TenantLogoUploadUrlResponse(uploadUrl, objectKey, 10);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> updateLogo(
            @Valid @RequestBody UpdateTenantLogoBody body
    ) throws AuthenticationFailedException {
        UUID tenantId = tenantContext.getRequiredClinicId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", tenantId));
        tenant.setLogoObjectKey(body.objectKey());
        tenantRepository.save(tenant);
        log.info("Logo da clínica {} atualizado: {}", tenantId, body.objectKey());
        return ResponseEntity.noContent().build();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private TenantResponse toResponse(Tenant t) {
        return new TenantResponse(
                t.getId(), t.getName(), t.getCnpj(), t.getPlanType(),
                t.getAddress(), t.getPhone(), t.isActive(), t.getSubdomain(),
                t.getType(), t.getStatus(), t.getTrialEndsAt(), t.getLogoObjectKey(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }

    private String resolveExtension(String fileName) {
        if (fileName == null) return ".png";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return ".jpg";
        if (lower.endsWith(".svg")) return ".svg";
        if (lower.endsWith(".webp")) return ".webp";
        return ".png";
    }
}
