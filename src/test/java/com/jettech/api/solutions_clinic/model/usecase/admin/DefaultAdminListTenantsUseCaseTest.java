package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.InvalidRequestException;
import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.SubscriptionRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAdminListTenantsUseCaseTest {

    @Mock TenantRepository tenantRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserTenantRoleRepository userTenantRoleRepository;

    @InjectMocks DefaultAdminListTenantsUseCase useCase;

    private Tenant tenant;
    private Subscription subscription;
    private UserTenantRole ownerRole;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", UUID.randomUUID());
        tenant.setName("Clínica Teste");
        tenant.setCnpj("12345678000199");
        tenant.setSubdomain("clinica-teste");
        tenant.setType(TypeTenant.CLINIC);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setPlanType(PlanType.BASIC);

        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        owner.setFirstName("João");
        owner.setLastName("Silva");
        owner.setEmail("joao@clinica.com");
        owner.setPhone("11999999999");

        subscription = new Subscription();
        ReflectionTestUtils.setField(subscription, "id", UUID.randomUUID());
        subscription.setTenant(tenant);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAmount(BigDecimal.valueOf(199.90));
        subscription.setCurrency("BRL");

        ownerRole = new UserTenantRole();
        ownerRole.setTenant(tenant);
        ownerRole.setUser(owner);
        ownerRole.setRole(Role.OWNER);
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenTenantsExist_thenReturnPageWithSubscriptionAndOwner() {
        var page = new PageImpl<>(List.of(tenant));
        when(tenantRepository.findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class))).thenReturn(page);
        when(subscriptionRepository.findLatestByTenantIds(any())).thenReturn(List.of(subscription));
        when(userTenantRoleRepository.findOwnersByTenantIds(any())).thenReturn(List.of(ownerRole));

        var result = useCase.execute(new AdminListTenantsRequest(0, 10, null, null));

        assertThat(result.getTotalElements()).isEqualTo(1);
        var item = result.getContent().get(0);
        assertThat(item.name()).isEqualTo("Clínica Teste");
        assertThat(item.subscription()).isNotNull();
        assertThat(item.subscription().status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(item.owner()).isNotNull();
        assertThat(item.owner().firstName()).isEqualTo("João");
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenFilterByStatus_thenReturnOnlyMatchingTenants() {
        var page = new PageImpl<>(List.of(tenant));
        when(tenantRepository.findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class))).thenReturn(page);
        when(subscriptionRepository.findLatestByTenantIds(any())).thenReturn(List.of());
        when(userTenantRoleRepository.findOwnersByTenantIds(any())).thenReturn(List.of());

        var result = useCase.execute(new AdminListTenantsRequest(0, 10, TenantStatus.ACTIVE, null));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenFilterByPlanType_thenReturnOnlyMatchingTenants() {
        var page = new PageImpl<>(List.of(tenant));
        when(tenantRepository.findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class))).thenReturn(page);
        when(subscriptionRepository.findLatestByTenantIds(any())).thenReturn(List.of());
        when(userTenantRoleRepository.findOwnersByTenantIds(any())).thenReturn(List.of());

        var result = useCase.execute(new AdminListTenantsRequest(0, 10, null, PlanType.BASIC));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).planType()).isEqualTo(PlanType.BASIC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenTenantHasNoSubscription_thenSubscriptionIsNull() {
        var page = new PageImpl<>(List.of(tenant));
        when(tenantRepository.findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class))).thenReturn(page);
        when(subscriptionRepository.findLatestByTenantIds(any())).thenReturn(List.of());
        when(userTenantRoleRepository.findOwnersByTenantIds(any())).thenReturn(List.of());

        var result = useCase.execute(new AdminListTenantsRequest(0, 10, null, null));

        assertThat(result.getContent().get(0).subscription()).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenTenantHasNoOwner_thenOwnerIsNull() {
        var page = new PageImpl<>(List.of(tenant));
        when(tenantRepository.findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class))).thenReturn(page);
        when(subscriptionRepository.findLatestByTenantIds(any())).thenReturn(List.of(subscription));
        when(userTenantRoleRepository.findOwnersByTenantIds(any())).thenReturn(List.of());

        var result = useCase.execute(new AdminListTenantsRequest(0, 10, null, null));

        assertThat(result.getContent().get(0).owner()).isNull();
    }

    @Test
    void whenPageSizeExceeds100_thenThrowInvalidRequestException() {
        assertThatThrownBy(() -> useCase.execute(new AdminListTenantsRequest(0, 101, null, null)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenNoTenantsExist_thenReturnEmptyPage() {
        when(tenantRepository.findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(subscriptionRepository.findLatestByTenantIds(any())).thenReturn(List.of());
        when(userTenantRoleRepository.findOwnersByTenantIds(any())).thenReturn(List.of());

        var result = useCase.execute(new AdminListTenantsRequest(0, 10, null, null));

        assertThat(result.isEmpty()).isTrue();
    }
}
