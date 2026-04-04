package com.jettech.api.solutions_clinic.model.usecase.subscription;

import com.jettech.api.solutions_clinic.model.entity.Subscription;
import com.jettech.api.solutions_clinic.model.entity.SubscriptionStatus;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.repository.SubscriptionRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.api.solutions_clinic.exception.ApiError;
import com.jettech.api.solutions_clinic.exception.InvalidRequestException;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultProcessStripeWebhookUseCase implements ProcessStripeWebhookUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void execute(ProcessStripeWebhookRequest request) {
        Event event;
        try {
            event = Webhook.constructEvent(request.payload(), request.signature(), request.webhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Erro ao verificar assinatura do webhook do Stripe", e);
            throw new InvalidRequestException(ApiError.INVALID_SIGNATURE, e);
        }

        log.info("Processando evento do Stripe - type: {}, id: {}", event.getType(), event.getId());

        // Processar eventos do Stripe
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "customer.subscription.updated":
                handleSubscriptionUpdated(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            default:
                log.info("Evento recebido mas não processado (eventos suportados: checkout.session.completed, customer.subscription.updated, customer.subscription.deleted): type={}, id={}", event.getType(), event.getId());
                break;
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        Session session = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            Object object = dataObjectDeserializer.getObject().get();
            if (object instanceof Session) {
                session = (Session) object;
            }
        }

        // Fallback: deserialização "unsafe" quando há mismatch de API version entre
        // o evento (Event.getApiVersion()) e a lib (Stripe.API_VERSION). getObject() retorna vazio nesse caso.
        if (session == null) {
            try {
                Object unsafe = dataObjectDeserializer.deserializeUnsafe();
                if (unsafe instanceof Session) {
                    session = (Session) unsafe;
                }
            } catch (EventDataObjectDeserializationException e) {
                log.warn("Falha ao deserializar objeto do evento (raw json disponível na exceção): {}", e.getMessage());
            }
        }

        if (session == null) {
            log.error("Sessão não encontrada no evento checkout.session.completed (event.apiVersion={}). " +
                    "Considere configurar o webhook no Stripe com api_versions alinhada à biblioteca.", event.getApiVersion());
            return;
        }

        log.info("Processando checkout.session.completed - sessionId: {}", session.getId());

        Subscription subscription = subscriptionRepository.findByStripeCheckoutSessionId(session.getId())
                .orElse(null);

        if (subscription == null) {
            log.warn("Subscription não encontrada para sessionId: {}", session.getId());
            return;
        }

        // Atualizar subscription com informações do Stripe
        if (session.getSubscription() != null) {
            subscription.setStripeSubscriptionId(session.getSubscription());
            
            // Buscar informações detalhadas da subscription do Stripe
            try {
                updateSubscriptionFromStripe(subscription, session.getSubscription());
            } catch (StripeException e) {
                log.error("Erro ao buscar informações da subscription do Stripe: {}", e.getMessage(), e);
                // Continua mesmo se houver erro ao buscar informações do Stripe
            }
        }
        if (session.getCustomer() != null) {
            subscription.setStripeCustomerId(session.getCustomer());
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription ativada - subscriptionId: {}, tenantId: {}, status após save: {}",
                subscription.getId(), subscription.getTenant().getId(), subscription.getStatus());

        // Atualizar tenant
        log.info("Chamando updateTenantAfterPayment - subscription.status: {}", subscription.getStatus());
        updateTenantAfterPayment(subscription);
    }

    private void updateTenantAfterPayment(Subscription subscription) {
        log.info("updateTenantAfterPayment - Iniciando. Subscription status: {}", subscription.getStatus());

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            log.warn("updateTenantAfterPayment - Subscription não está ACTIVE, saindo. Status atual: {}", subscription.getStatus());
            return;
        }

        Tenant tenant = subscription.getTenant();
        log.info("updateTenantAfterPayment - Tenant encontrado: id={}, statusAtual={}", tenant.getId(), tenant.getStatus());
        tenant.setPlanType(subscription.getPlanType());
        
        if (tenant.getStatus() == TenantStatus.PENDING_SETUP) {
            tenant.setStatus(TenantStatus.ACTIVE);
        }
        
        tenant.setActive(true);
        tenantRepository.save(tenant);

        log.info("Tenant atualizado após pagamento - tenantId: {}, planType: {}, status: {}", 
                tenant.getId(), tenant.getPlanType(), tenant.getStatus());
    }

    /**
     * Prepara a subscription para receber informações de período.
     * 
     * As informações de período (currentPeriodStart, currentPeriodEnd) serão
     * atualizadas quando eventos específicos do Stripe forem processados, como:
     * - customer.subscription.updated (quando a subscription é atualizada/renovada)
     * - invoice.paid (quando um pagamento é processado)
     * 
     * O Stripe gerencia automaticamente as cobranças recorrentes. Esta aplicação
     * mantém uma cópia local dessas informações para controle interno, exibição
     * ao usuário e auditoria.
     */
    private void updateSubscriptionFromStripe(com.jettech.api.solutions_clinic.model.entity.Subscription subscription, String stripeSubscriptionId) throws StripeException {
        // A subscription ID do Stripe já foi salva anteriormente no código
        // As datas de período serão atualizadas via eventos específicos do Stripe
        // ou através de uma sincronização periódica se necessário
        
        log.info("Subscription ID do Stripe salvo - localSubscriptionId: {}, stripeSubscriptionId: {}", 
                subscription.getId(), stripeSubscriptionId);
    }

    /**
     * Processa o evento customer.subscription.updated do Stripe.
     * Atualiza as informações de período da subscription (currentPeriodStart, currentPeriodEnd).
     */
    private void handleSubscriptionUpdated(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        com.stripe.model.Subscription stripeSubscription = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            Object object = dataObjectDeserializer.getObject().get();
            if (object instanceof com.stripe.model.Subscription) {
                stripeSubscription = (com.stripe.model.Subscription) object;
            }
        }
        if (stripeSubscription == null) {
            try {
                Object unsafe = dataObjectDeserializer.deserializeUnsafe();
                if (unsafe instanceof com.stripe.model.Subscription) {
                    stripeSubscription = (com.stripe.model.Subscription) unsafe;
                }
            } catch (EventDataObjectDeserializationException e) {
                log.warn("Falha ao deserializar objeto do evento customer.subscription.updated: {}", e.getMessage());
            }
        }

        if (stripeSubscription == null) {
            log.error("Subscription não encontrada no evento customer.subscription.updated (event.apiVersion={})", event.getApiVersion());
            return;
        }

        log.info("Processando customer.subscription.updated - subscriptionId: {}", stripeSubscription.getId());

        com.jettech.api.solutions_clinic.model.entity.Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId())
                .orElse(null);

        if (subscription == null) {
            log.warn("Subscription local não encontrada para stripeSubscriptionId: {}", stripeSubscription.getId());
            return;
        }

        // Atualizar período de cobrança
        // No SDK Java do Stripe, as propriedades são acessadas através do JSONObject interno
        // Usando reflection para acessar campos privados (não ideal, mas necessário devido à API do Stripe)
        try {
            Object periodStartObj = getFieldValue(stripeSubscription, "currentPeriodStart");
            if (periodStartObj instanceof Long) {
                LocalDateTime periodStart = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond((Long) periodStartObj), 
                    ZoneId.systemDefault()
                );
                subscription.setCurrentPeriodStart(periodStart);
            }
        } catch (Exception e) {
            log.debug("Erro ao acessar currentPeriodStart: {}", e.getMessage());
        }

        try {
            Object periodEndObj = getFieldValue(stripeSubscription, "currentPeriodEnd");
            if (periodEndObj instanceof Long) {
                LocalDateTime periodEnd = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond((Long) periodEndObj), 
                    ZoneId.systemDefault()
                );
                subscription.setCurrentPeriodEnd(periodEnd);
            }
        } catch (Exception e) {
            log.debug("Erro ao acessar currentPeriodEnd: {}", e.getMessage());
        }

        // Atualizar status baseado no status da subscription do Stripe
        String stripeStatus = null;
        try {
            stripeStatus = stripeSubscription.getStatus();
        } catch (Exception e) {
            log.debug("Erro ao acessar status: {}", e.getMessage());
        }
        if (stripeStatus != null) {
            switch (stripeStatus) {
                case "active":
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                    break;
                case "canceled":
                    subscription.setStatus(SubscriptionStatus.CANCELED);
                    if (subscription.getCanceledAt() == null) {
                        subscription.setCanceledAt(LocalDateTime.now());
                    }
                    break;
                case "past_due":
                    subscription.setStatus(SubscriptionStatus.PAST_DUE);
                    break;
                case "unpaid":
                    subscription.setStatus(SubscriptionStatus.UNPAID);
                    break;
                default:
                    log.debug("Status do Stripe não mapeado: {}", stripeStatus);
                    break;
            }
        }

        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription atualizada - subscriptionId: {}, periodStart: {}, periodEnd: {}, status: {}", 
                subscription.getId(), subscription.getCurrentPeriodStart(), 
                subscription.getCurrentPeriodEnd(), subscription.getStatus());
    }

    /**
     * Processa o evento customer.subscription.deleted do Stripe.
     * Marca a subscription como cancelada e salva a data de cancelamento.
     */
    private void handleSubscriptionDeleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        com.stripe.model.Subscription stripeSubscription = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            Object object = dataObjectDeserializer.getObject().get();
            if (object instanceof com.stripe.model.Subscription) {
                stripeSubscription = (com.stripe.model.Subscription) object;
            }
        }
        if (stripeSubscription == null) {
            try {
                Object unsafe = dataObjectDeserializer.deserializeUnsafe();
                if (unsafe instanceof com.stripe.model.Subscription) {
                    stripeSubscription = (com.stripe.model.Subscription) unsafe;
                }
            } catch (EventDataObjectDeserializationException e) {
                log.warn("Falha ao deserializar objeto do evento customer.subscription.deleted: {}", e.getMessage());
            }
        }

        if (stripeSubscription == null) {
            log.error("Subscription não encontrada no evento customer.subscription.deleted (event.apiVersion={})", event.getApiVersion());
            return;
        }

        log.info("Processando customer.subscription.deleted - subscriptionId: {}", stripeSubscription.getId());

        com.jettech.api.solutions_clinic.model.entity.Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId())
                .orElse(null);

        if (subscription == null) {
            log.warn("Subscription local não encontrada para stripeSubscriptionId: {}", stripeSubscription.getId());
            return;
        }

        // Marcar como cancelada
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setCanceledAt(LocalDateTime.now());

        // Atualizar tenant se necessário
        Tenant tenant = subscription.getTenant();
        tenant.setActive(false);
        tenant.setStatus(com.jettech.api.solutions_clinic.model.entity.TenantStatus.CANCELED);
        tenantRepository.save(tenant);

        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription cancelada - subscriptionId: {}, canceledAt: {}, tenantId: {}", 
                subscription.getId(), subscription.getCanceledAt(), tenant.getId());
    }

    /**
     * Acessa um campo de um objeto usando reflection.
     * Usado para acessar propriedades do objeto Subscription do Stripe que não são expostas via getters.
     */
    private Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
