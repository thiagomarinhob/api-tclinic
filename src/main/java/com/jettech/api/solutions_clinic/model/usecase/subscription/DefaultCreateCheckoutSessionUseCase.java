package com.jettech.api.solutions_clinic.model.usecase.subscription;

import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.Subscription;
import com.jettech.api.solutions_clinic.model.entity.SubscriptionStatus;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.SubscriptionRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.exception.DuplicateEntityException;
import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidRequestException;
import com.jettech.api.solutions_clinic.security.TenantContext;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCreateCheckoutSessionUseCase implements CreateCheckoutSessionUseCase {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantContext tenantContext;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${stripe.webhook.success.path:/plan-selection/success}")
    private String successPath;

    @Value("${stripe.webhook.cancel.path:/plan-selection}")
    private String cancelPath;

    private static final Map<PlanType, BigDecimal> PLAN_PRICES = new HashMap<>();
    private static final Map<PlanType, String> PLAN_NAMES = new HashMap<>();

    static {
        PLAN_PRICES.put(PlanType.SOLO, new BigDecimal("60.00"));
        PLAN_PRICES.put(PlanType.BASIC, new BigDecimal("299.00"));
        PLAN_PRICES.put(PlanType.PRO, new BigDecimal("599.00"));
        PLAN_NAMES.put(PlanType.SOLO, "Plano Solo");
        PLAN_NAMES.put(PlanType.BASIC, "Plano Básico");
        PLAN_NAMES.put(PlanType.PRO, "Plano Profissional");
    }

    @Override
    @Transactional
    public CreateCheckoutSessionResponse execute(CreateCheckoutSessionRequest request) throws AuthenticationFailedException {
        tenantContext.requireSameTenant(request.tenantId());
        log.info("Criando sessão de checkout - tenantId: {}, planType: {}", request.tenantId(), request.planType());

        if (request.planType() == PlanType.CUSTOM || request.planType() == PlanType.FREE) {
            throw new InvalidRequestException(ApiError.CUSTOM_PLAN_NO_CHECKOUT);
        }

        // Buscar tenant
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant", request.tenantId()));

        // Verificar se já existe assinatura ativa
        subscriptionRepository.findByTenantIdAndStatus(request.tenantId(), SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    throw new DuplicateEntityException(ApiError.DUPLICATE_SUBSCRIPTION);
                });

        // Obter preço do plano
        BigDecimal amount = PLAN_PRICES.get(request.planType());
        if (amount == null) {
            throw new InvalidRequestException(ApiError.PLAN_NOT_SUPPORTED);
        }

        String planName = PLAN_NAMES.get(request.planType());

        try {
            // Criar sessão de checkout do Stripe
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(frontendUrl + successPath + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + cancelPath)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("brl")
                                                    .setUnitAmount(amount.multiply(new BigDecimal("100")).longValue()) // Converter para centavos
                                                    .setRecurring(
                                                            SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                    .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                    .build()
                                                    )
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(planName)
                                                                    .setDescription("Assinatura mensal - " + planName)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("tenant_id", request.tenantId().toString())
                    .putMetadata("plan_type", request.planType().name())
                    .build();

            Session session = Session.create(params);

            // Criar subscription no banco
            Subscription subscription = new Subscription();
            subscription.setTenant(tenant);
            subscription.setPlanType(request.planType());
            subscription.setStatus(SubscriptionStatus.PENDING);
            subscription.setStripeCheckoutSessionId(session.getId());
            subscription.setAmount(amount);
            subscription.setCurrency("BRL");

            subscription = subscriptionRepository.save(subscription);

            log.info("Sessão de checkout criada com sucesso - sessionId: {}, subscriptionId: {}", 
                    session.getId(), subscription.getId());

            return new CreateCheckoutSessionResponse(session.getUrl(), session.getId());
        } catch (StripeException e) {
            log.error("Erro ao criar sessão de checkout do Stripe", e);
            throw new RuntimeException("Erro ao criar sessão de pagamento: " + e.getMessage(), e);
        }
    }
}
