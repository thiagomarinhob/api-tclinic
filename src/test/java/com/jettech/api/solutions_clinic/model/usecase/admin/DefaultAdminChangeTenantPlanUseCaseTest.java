package com.jettech.api.solutions_clinic.model.usecase.admin;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.exception.InvalidStateException;
import com.jettech.api.solutions_clinic.model.entity.PlanType;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.entity.TypeTenant;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAdminChangeTenantPlanUseCaseTest {

    @Mock TenantRepository tenantRepository;
    @InjectMocks DefaultAdminChangeTenantPlanUseCase useCase;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", tenantId);
        tenant.setName("Clínica ABC");
        tenant.setType(TypeTenant.CLINIC);
        tenant.setPlanType(PlanType.BASIC);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-uuid", null, List.of()));
    }

    @Test
    void whenTenantIsActive_thenChangePlanSuccessfully() {
        tenant.setStatus(TenantStatus.ACTIVE);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        useCase.execute(new AdminChangeTenantPlanRequest(tenantId, PlanType.PRO));

        assertThat(tenant.getPlanType()).isEqualTo(PlanType.PRO);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void whenTenantIsSuspended_thenChangePlanSuccessfully() {
        tenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        useCase.execute(new AdminChangeTenantPlanRequest(tenantId, PlanType.SOLO));

        assertThat(tenant.getPlanType()).isEqualTo(PlanType.SOLO);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void whenTenantIsTrial_thenChangePlanSuccessfully() {
        tenant.setStatus(TenantStatus.TRIAL);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        useCase.execute(new AdminChangeTenantPlanRequest(tenantId, PlanType.PRO));

        assertThat(tenant.getPlanType()).isEqualTo(PlanType.PRO);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void whenTenantIsCanceled_thenThrowInvalidStateException() {
        tenant.setStatus(TenantStatus.CANCELED);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> useCase.execute(new AdminChangeTenantPlanRequest(tenantId, PlanType.PRO)))
                .isInstanceOf(InvalidStateException.class);
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void whenTenantNotFound_thenThrowEntityNotFoundException() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AdminChangeTenantPlanRequest(tenantId, PlanType.PRO)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
