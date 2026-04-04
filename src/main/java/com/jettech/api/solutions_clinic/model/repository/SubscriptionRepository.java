package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Subscription;
import com.jettech.api.solutions_clinic.model.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    Optional<Subscription> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
    
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    Optional<Subscription> findByTenantIdAndStatus(UUID tenantId, SubscriptionStatus status);
    
    Optional<Subscription> findFirstByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
