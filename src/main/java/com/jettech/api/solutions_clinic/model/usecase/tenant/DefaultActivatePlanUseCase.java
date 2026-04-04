package com.jettech.api.solutions_clinic.model.usecase.tenant;

import com.jettech.api.solutions_clinic.model.entity.Subscription;
import com.jettech.api.solutions_clinic.model.entity.SubscriptionStatus;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.repository.SubscriptionRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UseCase para ativar um plano manualmente (para testes/desenvolvimento).
 * Este endpoint NÃO deve ser usado em produção - use o fluxo do Stripe para pagamentos reais.
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultActivatePlanUseCase implements ActivatePlanUseCase {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public TenantResponse execute(ActivatePlanRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        log.warn("ATIVACAO MANUAL DE PLANO - Este endpoint deve ser usado apenas para testes!");

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.tenantId()));

        log.info("Ativando plano manualmente - tenantId: {}, planType: {}", request.tenantId(), request.planType());

        // Atualizar o plano
        tenant.setPlanType(request.planType());
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setActive(true);
        tenant = tenantRepository.save(tenant);

        // Criar ou atualizar subscription
        Subscription subscription = subscriptionRepository.findFirstByTenantIdOrderByCreatedAtDesc(tenant.getId())
                .orElse(new Subscription());

        subscription.setTenant(tenant);
        subscription.setPlanType(request.planType());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAmount(getAmountForPlan(request.planType()));
        subscription.setCurrency("BRL");
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscription.setStripeSubscriptionId("manual_test_" + System.currentTimeMillis());
        subscription.setStripeCustomerId("manual_test_customer_" + tenant.getId());

        subscriptionRepository.save(subscription);

        log.info("Plano ativado com sucesso - tenantId: {}, planType: {}, status: {}",
                tenant.getId(), tenant.getPlanType(), tenant.getStatus());

        return toResponse(tenant);
    }

    private BigDecimal getAmountForPlan(com.jettech.api.solutions_clinic.model.entity.PlanType planType) {
        return switch (planType) {
            case FREE -> new BigDecimal("0.00");
            case SOLO -> new BigDecimal("60.00");
            case BASIC -> new BigDecimal("299.00");
            case PRO -> new BigDecimal("599.00");
            case CUSTOM -> new BigDecimal("0.00");
        };
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
