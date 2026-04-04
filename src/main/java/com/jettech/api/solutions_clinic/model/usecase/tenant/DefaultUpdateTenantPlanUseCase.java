package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultUpdateTenantPlanUseCase implements UpdateTenantPlanUseCase {

    private final TenantRepository tenantRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public TenantResponse execute(UpdateTenantPlanRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));
        log.info("id clinica {}", request.tenantId());

        // Atualizar o plano
        // Nota: Para ativar o plano via pagamento, use o endpoint de checkout
        // Este endpoint apenas atualiza o tipo de plano, mas não ativa o tenant
        // A ativação é feita automaticamente quando o pagamento é aprovado via webhook
        tenant.setPlanType(request.planType());

        // Se for CUSTOM, mantém PENDING_SETUP até aprovação manual
        // Para outros planos, a ativação será feita pelo webhook do Stripe quando o pagamento for aprovado

        tenant = tenantRepository.save(tenant);

        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCnpj(),
                tenant.getPlanType(),
                tenant.getAddress(),
                tenant.getPhone(),
                tenant.isActive(),
                tenant.getSubdomain(),
                tenant.getType(),
                tenant.getStatus(),
                tenant.getTrialEndsAt(),
                tenant.getLogoObjectKey(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
