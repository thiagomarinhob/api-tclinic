package com.jettech.api.solutions_clinic.model.repository;

import com.jettech.api.solutions_clinic.model.entity.Subscription;
import com.jettech.api.solutions_clinic.model.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    Optional<Subscription> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
    
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    Optional<Subscription> findByTenantIdAndStatus(UUID tenantId, SubscriptionStatus status);
    
    Optional<Subscription> findFirstByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    @Query("SELECT s FROM subscriptions s WHERE s.tenant.id IN :tenantIds AND s.createdAt = (SELECT MAX(s2.createdAt) FROM subscriptions s2 WHERE s2.tenant.id = s.tenant.id)")
    List<Subscription> findLatestByTenantIds(@Param("tenantIds") List<UUID> tenantIds);
}
