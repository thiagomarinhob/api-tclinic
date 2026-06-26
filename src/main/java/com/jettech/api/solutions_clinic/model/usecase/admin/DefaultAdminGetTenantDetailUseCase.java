package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.Role;
import com.jettech.api.solutions_clinic.model.entity.Subscription;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.SubscriptionRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultAdminGetTenantDetailUseCase implements AdminGetTenantDetailUseCase {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminTenantDetailResponse execute(UUID tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant não encontrado: " + tenantId));

        Subscription sub = subscriptionRepository
                .findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElse(null);

        UserTenantRole ownerRole = userTenantRoleRepository
                .findFirstByTenant_IdAndRoleOrderByCreatedAtAsc(tenantId, Role.OWNER)
                .orElse(null);

        AdminOwnerResponse owner = ownerRole == null ? null : new AdminOwnerResponse(
                ownerRole.getUser().getId(),
                ownerRole.getUser().getFirstName(),
                ownerRole.getUser().getLastName(),
                ownerRole.getUser().getEmail(),
                ownerRole.getUser().getPhone()
        );

        AdminSubscriptionDetailResponse subscription = sub == null ? null : new AdminSubscriptionDetailResponse(
                sub.getId(),
                sub.getStatus(),
                sub.getAmount(),
                sub.getCurrency(),
                sub.getCurrentPeriodStart(),
                sub.getCurrentPeriodEnd(),
                sub.getCanceledAt(),
                sub.getStripeSubscriptionId(),
                sub.getStripeCustomerId(),
                sub.getStripeCheckoutSessionId(),
                sub.getCreatedAt()
        );

        return new AdminTenantDetailResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCnpj(),
                tenant.getSubdomain(),
                tenant.getAddress(),
                tenant.getPhone(),
                tenant.getType(),
                tenant.getStatus(),
                tenant.getPlanType(),
                tenant.getTrialEndsAt(),
                tenant.getLogoObjectKey(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt(),
                owner,
                subscription
        );
    }
}
