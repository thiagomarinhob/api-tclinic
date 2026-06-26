package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.InvalidRequestException;
import com.jettech.api.solutions_clinic.model.entity.Subscription;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.SubscriptionRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantSpecifications;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultAdminListTenantsUseCase implements AdminListTenantsUseCase {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserTenantRoleRepository userTenantRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminTenantListItemResponse> execute(AdminListTenantsRequest request) {
        if (request.page() < 0 || request.size() < 1 || request.size() > 100) {
            throw new InvalidRequestException("Parâmetros de paginação inválidos: page >= 0, 1 <= size <= 100");
        }

        var pageable = PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
        Specification<Tenant> spec = TenantSpecifications.byStatus(request.status())
                .and(TenantSpecifications.byPlanType(request.planType()));

        Page<Tenant> tenantPage = tenantRepository.findAll(spec, pageable);

        List<UUID> tenantIds = tenantPage.map(Tenant::getId).toList();

        Map<UUID, Subscription> subscriptionByTenantId = subscriptionRepository
                .findLatestByTenantIds(tenantIds)
                .stream()
                .collect(Collectors.toMap(s -> s.getTenant().getId(), s -> s, (a, b) -> a));

        Map<UUID, UserTenantRole> ownerByTenantId = userTenantRoleRepository
                .findOwnersByTenantIds(tenantIds)
                .stream()
                .collect(Collectors.toMap(utr -> utr.getTenant().getId(), utr -> utr, (a, b) -> a));

        return tenantPage.map(tenant -> toListItem(tenant, subscriptionByTenantId.get(tenant.getId()),
                ownerByTenantId.get(tenant.getId())));
    }

    private AdminTenantListItemResponse toListItem(Tenant tenant, Subscription sub, UserTenantRole ownerRole) {
        AdminOwnerResponse owner = ownerRole == null ? null : new AdminOwnerResponse(
                ownerRole.getUser().getId(),
                ownerRole.getUser().getFirstName(),
                ownerRole.getUser().getLastName(),
                ownerRole.getUser().getEmail(),
                ownerRole.getUser().getPhone()
        );

        AdminSubscriptionSummary subscription = sub == null ? null : new AdminSubscriptionSummary(
                sub.getStatus(),
                sub.getAmount(),
                sub.getCurrency(),
                sub.getCurrentPeriodStart(),
                sub.getCurrentPeriodEnd()
        );

        return new AdminTenantListItemResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCnpj(),
                tenant.getSubdomain(),
                tenant.getType(),
                tenant.getStatus(),
                tenant.getPlanType(),
                tenant.getTrialEndsAt(),
                tenant.getCreatedAt(),
                owner,
                subscription
        );
    }
}
