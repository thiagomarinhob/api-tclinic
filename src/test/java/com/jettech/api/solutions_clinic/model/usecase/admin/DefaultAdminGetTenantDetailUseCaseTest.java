package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.*;
import com.jettech.api.solutions_clinic.model.repository.SubscriptionRepository;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAdminGetTenantDetailUseCaseTest {

    @Mock TenantRepository tenantRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserTenantRoleRepository userTenantRoleRepository;

    @InjectMocks DefaultAdminGetTenantDetailUseCase useCase;

    private UUID tenantId;
    private Tenant tenant;
    private Subscription subscription;
    private UserTenantRole ownerRole;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        tenant.setName("Clínica XYZ");
        tenant.setType(TypeTenant.CLINIC);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setPlanType(PlanType.PRO);

        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        owner.setFirstName("Maria");
        owner.setLastName("Santos");
        owner.setEmail("maria@xyz.com");
        owner.setPhone("21988888888");

        subscription = new Subscription();
        ReflectionTestUtils.setField(subscription, "id", UUID.randomUUID());
        subscription.setTenant(tenant);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAmount(BigDecimal.valueOf(399.90));
        subscription.setCurrency("BRL");
        subscription.setStripeSubscriptionId("sub_123");
        subscription.setStripeCustomerId("cus_456");
        subscription.setStripeCheckoutSessionId("cs_789");

        ownerRole = new UserTenantRole();
        ownerRole.setTenant(tenant);
        ownerRole.setUser(owner);
        ownerRole.setRole(Role.OWNER);
    }

    @Test
    void whenTenantExists_thenReturnDetailWithStripeIds() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(subscriptionRepository.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)).thenReturn(Optional.of(subscription));
        when(userTenantRoleRepository.findFirstByTenant_IdAndRoleOrderByCreatedAtAsc(tenantId, Role.OWNER)).thenReturn(Optional.of(ownerRole));

        var result = useCase.execute(tenantId);

        assertThat(result.id()).isEqualTo(tenantId);
        assertThat(result.name()).isEqualTo("Clínica XYZ");
        assertThat(result.subscription()).isNotNull();
        assertThat(result.subscription().stripeSubscriptionId()).isEqualTo("sub_123");
        assertThat(result.subscription().stripeCustomerId()).isEqualTo("cus_456");
        assertThat(result.subscription().stripeCheckoutSessionId()).isEqualTo("cs_789");
        assertThat(result.owner()).isNotNull();
        assertThat(result.owner().firstName()).isEqualTo("Maria");
    }

    @Test
    void whenTenantNotFound_thenThrowEntityNotFoundException() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(tenantId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenTenantHasNoSubscription_thenSubscriptionIsNull() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(subscriptionRepository.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)).thenReturn(Optional.empty());
        when(userTenantRoleRepository.findFirstByTenant_IdAndRoleOrderByCreatedAtAsc(tenantId, Role.OWNER)).thenReturn(Optional.of(ownerRole));

        var result = useCase.execute(tenantId);

        assertThat(result.subscription()).isNull();
    }

    @Test
    void whenTenantHasNoOwner_thenOwnerIsNull() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(subscriptionRepository.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)).thenReturn(Optional.of(subscription));
        when(userTenantRoleRepository.findFirstByTenant_IdAndRoleOrderByCreatedAtAsc(tenantId, Role.OWNER)).thenReturn(Optional.empty());

        var result = useCase.execute(tenantId);

        assertThat(result.owner()).isNull();
    }
}
